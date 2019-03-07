# orderddos

## Web frontend

```bash
$ cd order-receiver
$ npm install
$ ./node_modules/.bin/webpack
$ ORDER_DDOS_CFG=../config/whatever.json npm start
```

## Digital ocean droplet bandwidth

Тест на минимальной виртуалке пропускной способности сети: 

```text
root@bandwith-test:~/wrk# wrk -t1 -d1h -c500 https://order-ddos.com/
Running 60m test @ https://order-ddos.com/
  1 threads and 500 connections
    Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    42.91ms   68.59ms   1.71s    90.49%
    Req/Sec    17.52k     2.18k   23.43k    70.91%
  5094212 requests in 4.88m, 38.78GB read
  Socket errors: connect 0, read 0, write 0, timeout 1
Requests/sec:  17406.26
Transfer/sec:    135.69MB
```

Digital Ocean говорит что узел можнт выдавать до 1 Gbit/s, что и наблюдается. 