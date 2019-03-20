const path = require('path');

const config = require(process.env.ORDER_DDOS_CFG);

const http = require('http');

const express = require('express');
const pgPromise = require('pg-promise')();

const app = express();
const db = pgPromise(config.db);

var exphbs = require('express-handlebars');

const hbsInstance = exphbs.create({
    helpers: {
        ifEq: (a, b, options) => a === b ? options.fn(this) : options.inverse(this)
    }
});

// app.engine('html', exphbs({ defaultLayout: 'main' }));
app.engine('html', hbsInstance.engine);
app.set('view engine', 'handlebars');
app.set('views', path.join(__dirname, 'views'));

app.use(express.json());

const zeroPad = (x, l) => {
    x = x.toString();

    while(x.length < l) {
        x = '0' + x;
    }

    return x;
};

const formatDateTime = dt => {
    return `${zeroPad(dt.getFullYear(), 4)}-${zeroPad(dt.getMonth() + 1, 2)}-${zeroPad(dt.getDate(), 2)}`
        + ` ${zeroPad(dt.getHours(), 2)}:${zeroPad(dt.getMinutes(), 2)}:${zeroPad(dt.getSeconds(), 2)}`
};

const formatTimezone = tzOffset => {
    if(tzOffset < 0) {
        return `UTC+${Math.round(-tzOffset / 60)}`;
    }
    else if(tzOffset === 0) {
        return "UTC";
    }
    else {
        return `UTC-${Math.round(tzOffset / 60)}`;
    }
}

app.get('/order/:id', (req, res) => {
    db.one(
        `SELECT * FROM Orders WHERE uuid=$1`,
        [req.params.id]
    ).then(data => {
        let duration = 0;

        if(data.duration.hours) duration += 60 * data.duration.hours;
        if(data.duration.minutes) duration += data.duration.minutes;

        // TODO: display times, except startTime, in the client's timezone (UTC for no-JS clients)
        res.render("order-details.html", {
            uuid: data.uuid,
            status: data.status,
            submissionTime: formatDateTime(data.t_submitted),
            submissionTz: formatTimezone(data.t_submitted.getTimezoneOffset()),
            email: data.email,
            targetUrl: data.target_url,
            numNa: data.num_nodes_by_region.na,
            numEu: data.num_nodes_by_region.eu,
            numA: data.num_nodes_by_region.as,
            duration: duration,
            startTime: data.t_start ? formatDateTime(data.t_start) : null,
            startTz: data.t_start ? formatTimezone(data.t_start.getTimezoneOffset()) : ""
        });
    }).catch(err => {
        res.status(404).send(`<h2>Not found</h2>
            <p><pre style="color: red;">${err}</pre>
            <p><a href="/order">Back to order form</a>`);
    });
});

app.get('/cancel/:id', (req, res) => {
    console.log("Cancellation request", req.params.id);

    http.request({
      host: 'google.com',
      path: '/' + req.params.id,
      port: '80',
      method: 'POST'
    }, response => {
        response.on('data', hz => {
            console.log("Cancellation response for", req.params.id);
            console.log(data);
        });
    });

    res.send(JSON.stringify({ status: 'OK' }));
});

app.post('/submit-order', (req, res) => {
    // TODO: process urlencoded requests separately for no-JS clients

    const { email, targetUrl, numNa, numEu, numA, duration, startTime } = req.body;

    // NOTE: use the default for t_submitted as soon as it's fixed in the schema
    db.one(
        `INSERT INTO Orders (t_submitted, email, target_url, num_nodes_by_region, t_start, duration)
         VALUES ($1, $2, $3, $4, $5, $6) RETURNING uuid`,
        [
            new Date(),
            email,
            targetUrl,
            JSON.stringify({
                na: parseInt(numNa),
                eu: parseInt(numEu),
                as: parseInt(numA)
            }),
            startTime,
            duration + ' minutes'
        ]
    ).then(data => {
        console.log(`New order: \x1b[1m${data.uuid}\x1b[0m`);

        res.status(201).send(JSON.stringify({
            status: 'OK',
            location: `/order/${data.uuid}`
        }));
    }).catch(err => {
        console.log("Ept:", err);
        res.status(400).send(`<h2>Че за хуйня?</h2>
            <p><pre style="color: red;">${JSON.stringify(err, null, 4)}</pre>
            <p><a href="/order">Back to order form</a>`);
    });
});

app.get('/', (req, res) => {
    res.render("index.html");
});

app.get('/order', (req, res) => {
    res.render("create-order.html");
});

app.use(express.static(path.join(__dirname, 'dist')));
app.use(express.static(path.join(__dirname, 'static')));

app.listen(config.web.port, () => console.log(`Listening on port \x1b[1m${config.web.port}\x1b[0m.`))
