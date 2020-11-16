const utils = require('@sap/approuter/lib/passport/utils')
const headerUtil = require('@sap/approuter/lib/utils/header-util');
const urlUtils = require('@sap/approuter/lib/utils/url-utils');
const uaaUtils = require('@sap/approuter/lib/utils/uaa-utils');

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
