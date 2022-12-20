package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;


public class RemoteDriver {

	private static final Logger log = LoggerFactory.getLogger(RemoteDriver.class);

	public static void main(String[] args) throws MalformedURLException, InterruptedException {
		LocalDriver.setup(log,"remote");
	}
}