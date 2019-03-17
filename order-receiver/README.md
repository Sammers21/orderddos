## `order-receiver`

Web frontend for the processing of incoming orders.

### Prerequisites

- Node.js 11

### Manual deployment

1. Install npm dependencies:

   ```bash
   $ npm install
   ```

2. Bundle the client files:

   ```bash
   $ ./node_modules/.bin/webpack
   ```

3. Launch the web server:

   ```bash
   $ ORDER_DDOS_CFG=../config/whatever.json npm start
   ```

   *(this assumes `../config/whatever.json` to be filled in with production settings)*

4. Optionally, start several instances behind nginx as proxy.
