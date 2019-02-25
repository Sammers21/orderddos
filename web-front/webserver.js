const path = require('path');

const config = require(process.env.ORDER_DDOS_CFG);

const express = require('express');
const pgPromise = require('pg-promise')();

const app = express();
const db = pgPromise(config.db);

var exphbs  = require('express-handlebars');

// app.engine('html', exphbs({ defaultLayout: 'main' }));
app.engine('html', exphbs({ }));
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

app.get('/order/:id', (req, res) => {
    db.one(
        `SELECT * FROM Orders WHERE uuid=$1`,
        [req.params.id]
    ).then(data => {
        let duration = 0;

        if(data.duration.hours) duration += 60 * data.duration.hours;
        if(data.duration.minutes) duration += data.duration.minutes;

        let submissionTz = -data.t_submitted.getTimezoneOffset() / 60;

        if(submissionTz > 0) {
            submissionTz = '+' + submissionTz;
        }
        else if(submissionTz === 0) {
            submissionTz = "";
        }

        res.render("order-details.html", {
            uuid: data.uuid,
            submissionTime: formatDateTime(data.t_submitted),
            submissionTz: submissionTz,
            email: data.email,
            targetUrl: data.target_url,
            numNa: data.num_nodes_by_region.na,
            numEu: data.num_nodes_by_region.eu,
            numA: data.num_nodes_by_region.as,
            duration: duration
        });

        // res.send(`<h2>Order ${req.params.id}</h2>
        //     <p><pre>${JSON.stringify(data, null, 4)}</pre>
        //     <p><a href="/order">Back to order form</a>`);
    }).catch(err => {
        res.status(404).send(`<h2>Not found</h2>
            <p><pre style="color: red;">${err}</pre>
            <p><a href="/order">Back to order form</a>`);
    });
});

app.post('/submit-order', (req, res) => {
    const { email, targetUrl, numNa, numEu, numA, duration, startTime } = req.body;

    // TODO: process urlencoded requests separately for no-JS clients

    // FIX: time gets written with current time zone, but in UTC

    db.one(
        `INSERT INTO Orders (email, target_url, num_nodes_by_region, t_start, duration)
         VALUES ($1, $2, $3, $4, $5) RETURNING uuid`,
        [
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
    res.sendFile("dist/home.html", { root: __dirname });
});

app.get('/order', (req, res) => {
    res.sendFile("dist/order.html", { root: __dirname });
});

app.use(express.static(path.join(__dirname, 'dist')));
app.use(express.static(path.join(__dirname, 'static')));

app.listen(config.web.port, () => console.log(`Listening on port \x1b[1m${config.web.port}\x1b[0m.`))
