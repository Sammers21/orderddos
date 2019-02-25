
distribute: dist dist/js/order.js dist/order.html dist/home.html

dist:
	mkdir -p dist

dist/js/order.js: src/js/order.jsx src/js/components/*.jsx
	./node_modules/.bin/webpack

dist/order.html:
	cp ./src/order.html ./dist

dist/home.html:
	cp ./src/home.html ./dist

clean:
	rm -r ./dist

.PHONY: distribute clean
