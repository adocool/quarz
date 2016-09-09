package quarz.job;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZeroJob implements IJob {

    private static final Logger log = LoggerFactory.getLogger(HourJob.class);

    @Override
    public void execute(JobExecutionContext arg0) throws JobExecutionException {
        long time = System.currentTimeMillis();

        long finish = System.currentTimeMillis();
        log.warn("0点调度结算排位赛成功 cost time" + (finish - time));

        log.warn("assayZeorJob cost time" + (System.currentTimeMillis() - finish));
    }

    @Override
    public String getCronExpression() {
        return "0 0 0 * * ?";
    }

}
