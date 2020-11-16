const encodeUrl = require('encodeurl');
const utils = require('@sap/approuter/lib/passport/utils')
const headerUtil = require('@sap/approuter/lib/utils/header-util');
const urlUtils = require('@sap/approuter/lib/utils/url-utils');
const uaaUtils = require('@sap/approuter/lib/utils/uaa-utils');
const logoutProvider = require('@sap/approuter/lib/middleware/logout-provider');

utils.loadOauthOptions = function (req, cb) {
  uaaUtils.getUaaConfig(req, function (err, uaaOptions) {
    if (err) {
      return cb(err);
    }

    var uaaUrl = uaaOptions.url;
    var options = {
      authorizationURL: urlUtils.join(uaaUrl, '/protocol/openid-connect/auth'),
      tokenURL: urlUtils.join(uaaUrl, '/protocol/openid-connect/token'),
      clientid: uaaOptions.clientid,
      clientsecret: uaaOptions.clientsecret,
      url: uaaUrl,
      callbackURL: process.env['CALLBACK_URL'] || utils.getCallBackUrl(req),
      customHeaders: {}
    };

    headerUtil.updateSapPassport(req.headers, options.customHeaders);
    cb(null, options);
  });
}

logoutProvider.triggerUAARedirect = function(req, res, cb) {
  resolveLogoutRedirectUrl(req, function (err, redirectUrl) {
    if (!err) {
      res.writeHead(302, {Location: encodeUrl(redirectUrl)});
      res.end();
    }
    cb(err);
  });
}

function resolveLogoutRedirectUrl(req, cb) {
  // Currently redirect from UAA to application is possible only with port-based routing
  const logout = req.routerConfig.appConfig.logout;
  uaaUtils.getUaaConfig(req, function (err, uaaConfig) {
    if (err) { return cb(err); }

    const redirectUrl = urlUtils.join(uaaConfig.url, '/protocol/openid-connect/logout');
    return cb(null, redirectUrl);
  });
}
