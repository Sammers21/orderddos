const path = require('path');

module.exports = {
    entry: './src/js/order.jsx',
    // mode: 'development',
    mode: 'production',
    watch: false,
    // stats: 'verbose',
    output: {
        path: path.resolve(__dirname, 'dist'),
        filename: 'js/order.js'
    },
    module: {
        rules: [
            {
                test: /\.(js|jsx)$/,
                exclude: /node_modules/,
                loader: 'babel-loader',
                options: {
                    presets: [
                        [
                            '@babel/env',
                            {
                                'targets': {
                                    'browsers': ['Chrome >=59']
                                },
                                'modules': false,
                                'loose': true
                            }
                        ],
                        [
                            '@babel/preset-react',
                            {
                                'development': true
                            }
                        ]
                    ],
                }
            }
        ]
    },
    resolve: {
        extensions: ['.js', '.jsx']
    }
};
