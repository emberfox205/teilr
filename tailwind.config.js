/** @type {import('tailwindcss').Config} */
module.exports = {
    content: [
        "./src/main/resources/templates/**/*.html",
        "./src/main/resources/static/**/*.js"
    ],
    theme: {
        extend: {
            colors: {
                brandPrimary: '#0f172a', /* Example Design System Tokens */
                brandAccent: '#3b82f6'
            },
            borderRadius: {
                'mobile-card': '16px'
            }
        },
    },
    plugins: [],
}