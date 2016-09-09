package quarz;

import common.ClassUtil;

import org.apache.log4j.xml.DOMConfigurator;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.Date;
import java.util.Set;

import quarz.job.IJob;

public class QuartzManager {

    private static final String QUARTZ = "config/quartz.properties";
    private static final String SCAN_PACKAGE = "quarz.job";
    private static final Logger log = LoggerFactory.getLogger(QuartzManager.class);

    private QuartzManager() {}

    private static QuartzManager instance = new QuartzManager();

    public static QuartzManager getInstance() {
        return instance;
    }

    private Scheduler scheduler;

    public void start() {
        try {
            SchedulerFactory sf = new StdSchedulerFactory(QUARTZ);
            scheduler = sf.getScheduler();
            initJob();
            scheduler.start();
        } catch (SchedulerException e) {
            log.error("Quartz scheduler start error", e);
            System.exit(1);
        }
    }

    public void shutDown() throws Exception {
        scheduler.shutdown();
        log.warn("Quartz shunDown OK");
    }

    private void initJob() {
        try {
            Set<Class<?>> cls = ClassUtil.getAllClassByPackStr(SCAN_PACKAGE, false);
            for (Class<?> c : cls) {
                if (c.isInterface()) {
                    continue;
                }
                try {
                    Object o = c.newInstance();
                    if (o instanceof IJob) {
                        addScheJob((IJob) o);
                    }
                } catch (InstantiationException e) {
                    e.printStackTrace();
                    throw new Exception();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    throw new Exception();
                } catch (ParseException e) {
                    e.printStackTrace();
                    throw new Exception();
                } catch (SchedulerException e) {
                    e.printStackTrace();
                    throw new Exception();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

    }

    /**
     * 触发cron调度
     * @param  callback
     *            Cron时间规则
     * @throws SchedulerException
     */

    private void addScheJob(IJob callback) throws ParseException, SchedulerException {

        String taskName = callback.toString();
        String group = "group";
        JobDetail job =
                JobBuilder.newJob(callback.getClass()).withIdentity(taskName, group).build();
        if (callback.getCronExpression() == null || callback.getCronExpression().isEmpty()) {
            log.warn(job.getKey().toString() + " cronExpression() is empty ,please check code " +
                    callback.getCronExpression());
            return;
        }
        CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity("trigger_" + taskName, group)
                .withSchedule(CronScheduleBuilder.cronSchedule(callback.getCronExpression()))
                .build();
        Date ft = this.scheduler.scheduleJob(job, trigger);
        log.warn(job.getKey().toString() + " has been scheduled to run at: " + ft +
                " and repeat based on expression: " + trigger.getCronExpression());
    }

    public static void main(String[] args) {
        DOMConfigurator.configure("config/log4j.xml");
        QuartzManager.instance.start();
    }

}
