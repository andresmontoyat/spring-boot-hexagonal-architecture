function fn() {
  return {
    baseUrl: karate.properties['baseUrl'],
    writerToken: karate.properties['writerToken'],
    readerToken: karate.properties['readerToken']
  };
}
