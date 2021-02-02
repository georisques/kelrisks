// vue.config.js
const TerserPlugin  = require('terser-webpack-plugin');
const isProd = process.env.NODE_ENV === "production";

module.exports = {
    pages: {
        index: {
            entry: 'src/main.js',
            template: 'public/index.html',
            filename: 'index.html',
            title: 'Evaluez simplement et rapidement les risques de votre bien - errial.georisques.gouv.fr'
        }
    },
    configureWebpack: {
        resolve: {
            alias: {
                // '@': 'src',
            }
        },
        optimization: {
            minimize: true,
            minimizer: isProd ? [
              new TerserPlugin({
                terserOptions: {
                  compress: {
                    drop_console: true
                  },
                  output: {
                      comments: false
                  }
                }
              })
            ] : []
        }        
    }
};