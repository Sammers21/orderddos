const path = require('path');

const config = require(process.env.ORDER_DDOS_CFG);

const express = require('express');
const pgPromise = require('pg-promise')();

const app = express();
const db = pgPromise(config.db);

app.use(express.urlencoded({extended: false}));

app.get('/order/:id', (req, res) => {
    db.one(
        `SELECT * FROM Orders WHERE uuid=$1`,
        [req.params.id]
    ).then(data => {
        res.send(`<h2>Order ${req.params.id}</h2>
            <p><pre>${JSON.stringify(data, null, 4)}</pre>
            <p><a href="/">Go back</a>`);
    }).catch(err => {
        res.status(404).send(`<h2>Not found</h2>
            <p><pre style="color: red;">${err}</pre>
            <p><a href="/">Go back</a>`);
    });
});

app.post('/submit-order', (req, res) => {
    const { email, target_url, num_nodes_na, num_nodes_eu, num_nodes_a, start_time, duration } = req.body;

    db.one(
        `INSERT INTO Orders (email, target_url, num_nodes_by_region, t_start, duration)
         VALUES ($1, $2, $3, $4, $5) RETURNING uuid`,
        [
            email,
            target_url,
            JSON.stringify({
                na: parseInt(num_nodes_na),
                eu: parseInt(num_nodes_eu),
                as: parseInt(num_nodes_a)
            }),
            start_time,
            duration + ' minutes'
        ]
    ).then(data => {
        console.log(`New order: \x1b[1m${data.uuid}\x1b[0m`);
        res.redirect(`/order/${data.uuid}`);
    }).catch(err => {
        console.log("Ept:", err);
        res.status(400).send(`<h2>Че за хуйня?</h2>
            <p><pre style="color: red;">${JSON.stringify(err, null, 4)}</pre>
            <p><a href="/">Go back</a>`);
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
