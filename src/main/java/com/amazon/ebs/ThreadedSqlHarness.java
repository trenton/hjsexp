package com.amazon.ebs;

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.*;

/**
 */
public class ThreadedSqlHarness {
    final static Random RAND = new Random();

    private MysqlConnectionPoolDataSource dataSource;
    private int threadCount;

    public MysqlConnectionPoolDataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(MysqlConnectionPoolDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    void go() throws InterruptedException {
        // make work queue larger than number of threads, so we will always have more
        // work than we have workers
        LinkedBlockingQueue<Runnable> handoff = new LinkedBlockingQueue<Runnable>(threadCount * 4);

        // create pool and start threads so they're waiting to see things added to
        // the handoff queue
        ThreadPoolExecutor consumers = new ThreadPoolExecutor(threadCount, threadCount, 0L, TimeUnit.MILLISECONDS, handoff);
        consumers.prestartAllCoreThreads();

        JdbcTemplate jdbc = new JdbcTemplate(dataSource);

        final int testDurationSeconds = 7;
        final long endTime = System.currentTimeMillis() + testDurationSeconds * 1000;
        while (System.currentTimeMillis() < endTime) {
            // put will block until there's more room in the queue; to guard against the
            // wait-forever case, we have a hard limit of 60 seconds to wait to add it
//            handoff.offer(new NumberPrinter(), 60, TimeUnit.SECONDS);
            handoff.offer(new QueryJob(jdbc), 60, TimeUnit.SECONDS);
            Thread.sleep(2000);
        }

        consumers.shutdownNow();
    }

    static class NumberPrinter implements Runnable {
        @Override
        public void run() {
            System.out.printf("%s : %.10f\n", currentThread().getName(), Math.random());

            try {
                int sigma = 250;
                int mean = 500;

                final long delay = gaussianPositive(mean, sigma);
                sleep(delay);
            } catch (InterruptedException e) {
                // move along, move along
            }
        }
    }

    static class QueryJob implements Runnable {
        final JdbcTemplate jdbc;

        public QueryJob(JdbcTemplate jdbc) {
            this.jdbc = jdbc;
        }

        @Override
        public void run() {
            List results = jdbc.query("select * from foo", new FooTableMapper());
            for (Object result : results) {
                System.out.printf("%s\n", result);
            }
        }
    }

    private static class FooTableMapper implements RowMapper {
        @Override
        public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getString(1);
        }
    }

    private static long gaussianPositive(int mean, int sigma) {
        return Math.round(Math.abs(RAND.nextGaussian()) * sigma + mean);
    }

    public static void main(String[] args) throws SQLException, InterruptedException {
        // we don't need to register a driver; spring does that for us
//        SmartDataSource dataSource = new SingleConnectionDataSource("jdbc:mysql://localhost", "username", "password", false);
        MysqlConnectionPoolDataSource ds = new MysqlConnectionPoolDataSource();
        ds.setUrl("jdbc:mysql://localhost:4475/temp");
        ds.setUser("trenton");
        ds.setPassword("elephant");
        // setautocommit = 0

        final int threadCount = 1;

        ThreadedSqlHarness me = new ThreadedSqlHarness();
        me.setDataSource(ds);
        me.setThreadCount(threadCount);

        me.go();
    }
}
