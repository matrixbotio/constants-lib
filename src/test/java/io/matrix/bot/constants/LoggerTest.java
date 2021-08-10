package io.matrix.bot.constants;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.matrix.bot.constants.model.Error;
import io.matrix.bot.constants.model.LogData;
import io.matrix.bot.constants.model.MatrixException;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ArrayBlockingQueue;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.*;

class LoggerTest {

	@Test
	public void should_successfully_handle_log() throws JsonProcessingException, InterruptedException {
		// given
		final var host = "mockedHost";
		final var source = "mockedSource";
		final var message = "Mocked message";
		final var blockingQueue = new ArrayBlockingQueue<String>(1);
		final var logger = Logger.newLogger(s -> {
			blockingQueue.add(s);
			return null;
		}, host, source);
		// when
		logger.log(message);
		// then
		final var logDataString = blockingQueue.poll(5, SECONDS);
		final var logData = new ObjectMapper().readValue(logDataString, LogData.class);
		assertEquals(0, logData.getCode());
		assertEquals(host, logData.getHost());
		assertEquals(2, logData.getLevel());
		assertEquals(message, logData.getMessage());
		assertEquals(source, logData.getSource());
		assertNull(logData.getStack());
		assertTrue(logData.getTimestamp() > 0L);
		logger.waitAllThreads();
	}

	@Test
	public void should_successfully_handle_error() throws JsonProcessingException, InterruptedException {
		// given
		final var host = "mockedHost";
		final var source = "mockedSource";
		final var message = "Mocked message";
		final var blockingQueue = new ArrayBlockingQueue<String>(1);
		final var logger = Logger.newLogger(s -> {
			blockingQueue.add(s);
			return null;
		}, host, source);
		// when
		logger.error(new MatrixException(message));
		// then
		final var logDataString = blockingQueue.poll(5, SECONDS);
		final var logData = new ObjectMapper().readValue(logDataString, LogData.class);
		assertEquals(host, logData.getHost());
		assertEquals(4, logData.getLevel());
		assertEquals(message, logData.getMessage());
		assertEquals(source, logData.getSource());
		assertNotNull(logData.getStack());
		assertTrue(logData.getTimestamp() > 0L);
		logger.waitAllThreads();
	}

	@Test
	public void should_handle_persisting_exception() throws InterruptedException {
		// given
		final var host = "mockedHost";
		final var source = "mockedSource";
		final var blockingQueue = new ArrayBlockingQueue<String>(1);
		final var logger = Logger.newLogger(s -> {
			blockingQueue.add(s);
			throw new RuntimeException("Test");
		}, host, source);
		// when
		logger.log("Test message");
		// then
		blockingQueue.poll(5, SECONDS);
		logger.waitAllThreads();
	}

	@Test
	public void should_handle_null_log_message() throws JsonProcessingException, InterruptedException {
		// given
		final var host = "mockedHost";
		final var source = "mockedSource";
		final String message = null;
		final var blockingQueue = new ArrayBlockingQueue<String>(1);
		final var logger = Logger.newLogger(s -> {
			blockingQueue.add(s);
			return null;
		}, host, source);
		// when
		logger.log(message);
		// then
		final var logDataString = blockingQueue.poll(5, SECONDS);
		final var logData = new ObjectMapper().readValue(logDataString, LogData.class);
		assertEquals(0, logData.getCode());
		assertEquals(host, logData.getHost());
		assertEquals(2, logData.getLevel());
		assertNull(message);
		assertEquals(source, logData.getSource());
		assertNull(logData.getStack());
		assertTrue(logData.getTimestamp() > 0L);
		logger.waitAllThreads();
	}

	@Test
	public void should_handle_many_messages() {
		final var logger = Logger.newLogger(s -> {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		}, "host", "source");
		for (int i=0; i<1000; ++i) {
			logger.log(String.valueOf(i));
		}
		logger.waitAllThreads();
	}

}