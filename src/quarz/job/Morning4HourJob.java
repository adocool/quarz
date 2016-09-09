package quarz.job;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 早上4点执行的job
 */
public class Morning4HourJob implements IJob {

	private static final Logger log = LoggerFactory.getLogger(Morning4HourJob.class);

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {

	}

	@Override
	public String getCronExpression() {
		return "0 0 4 * * ?";
	}

}
