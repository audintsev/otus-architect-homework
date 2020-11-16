FROM node:12-slim

COPY src /app
WORKDIR /app
RUN npm install

ENTRYPOINT ["node", "-r", "/app/patch-approuter-oauth-options.js", "/app/node_modules/@sap/approuter/approuter.js"]
