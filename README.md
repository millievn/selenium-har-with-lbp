# LightBrowserProxyServer Example
## use with local driver
- Open `LocalDriver` file and replace `path/to/driver` to your driver path
- run `mvn install` to install dependence
- run `LocalDriver.main` to get log
- remove `options.addArguments("headless");` to see what happened to chrome.

## use with remote driver

- make sure docker is installed
- run `docker-compose up -d` to start selenium environment
- run `mvn install` to install dependence
- run `RemoteDriver.main` to get log

> 