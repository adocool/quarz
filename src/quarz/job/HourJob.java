package quarz.job;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HourJob implements IJob {

	private static final Logger log = LoggerFactory.getLogger(HourJob.class);

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		long time = System.currentTimeMillis();
	}

	@Override
	public String getCronExpression() {
		return "0 0 * * * ?";
	}

}
