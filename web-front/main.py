#coding: utf-8

import os

import tornado.httpserver
import tornado.web

from tornado.options import define, options

import pymysql.cursors

define("port", default=8888, help="run on the given port", type=int)


class HomeHandler(tornado.web.RequestHandler):
    def get(self):
        # self.write("Pidor!")
        self.render("index.html")

class SubmitOrderHandler(tornado.web.RequestHandler):
    def initialize(self, db):
        self.db = db

    q_insert_order = """
        INSERT INTO `Orders` (`email`, `target_url`, `region`, `num_nodes`, `duration`)
        VALUES (%s, %s, %s, %s, %s)
    """

    def post(self):
        with self.db.cursor() as cursor:
            cursor.execute(
                self.q_insert_order,
                (
                    self.get_argument('email'),
                    self.get_argument('target_url'),
                    self.get_argument('region'),
                    self.get_argument('num_nodes'),
                    self.get_argument('duration')
                )
            )

        self.db.commit()

        self.redirect("/")

if __name__ == "__main__":
    tornado.options.parse_command_line()

    # TODO: find an async way to use MySQL this or ditch Tornado
    connection = pymysql.connect(
        host='localhost',
        user='root',
        password='',
        db='dddos',
        # charset='utf8',
        cursorclass=pymysql.cursors.DictCursor
    )

    application = tornado.web.Application(
        [
            (r"/", HomeHandler),
            (r"/submit-order", SubmitOrderHandler, {'db': connection})
        ],
        template_path=os.path.join(os.path.dirname(__file__), "templates"),
        static_path=os.path.join(os.path.dirname(__file__), "static"),

        # TODO: enable conditionally:
        debug=True,
        autoreload=True
    )

    http_server = tornado.httpserver.HTTPServer(application)

    print("Will listen on \x1b[1m{:d}\x1b[0m...".format(options.port))

    http_server.listen(options.port)

    tornado.ioloop.IOLoop.current().start()
