
distribute: dist dist/js/order.js

dist:
	mkdir -p dist

dist/js/order.js: src/js/order.jsx src/js/components/*.jsx
	./node_modules/.bin/webpack

clean:
	rm -r ./dist

.PHONY: distribute clean
