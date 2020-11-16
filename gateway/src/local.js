const approuter = require('@sap/approuter')
const xsappConfig = require('./xs-app.json')

try {
  const localEnv = require('../local-env.json')
  Object.entries(localEnv).forEach(([k, v]) => {
    process.env[k] = typeof v === "string" ? v : JSON.stringify(v, null, " ")
  })
} catch (e) {
  // do nothing
}

approuter().start({
  xsappConfig,
})
