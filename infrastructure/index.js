exports.handler = function(event, context, callback) {
  console.log('Running index.handler');
  console.log('==================================');
  console.log('event', event);
  console.log('==================================');
  console.log('Stopping index.handler');

  callback(null, {
    statusCode: 200,
    body: "hello world",
    headers: {
      "Content-Type": "text/html; charset=utf-8"
    },
    isBase64Encoded: false
  });
};
