package quarz.job;

import org.quartz.Job;

public interface IJob extends Job {
    String getCronExpression();
}


