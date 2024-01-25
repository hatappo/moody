/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ['./src/**/*.{cljs,css}', './public/**/*.html'],
  darkMode: 'class',
  theme: {
    extend: {},
  },
  plugins: [require('rippleui')],
};
