package com.htjc.assemble.test;

import com.htjc.assemble.config.CliOptions;
import org.apache.commons.cli.*;
import org.junit.Assert;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by guilin on 2016/9/7.
 */
public class CommandLineTest {

    @Test
    public void test1() throws ParseException {
        // create the command line parser
        CommandLineParser parser = new PosixParser();
        Options options = new Options();
        options.addOption("a", "all", true, "do not hide entries starting with .");
        options.addOption("b", "escape", false, "print octal escapes for nongraphic characters");
        options.addOption(OptionBuilder.withLongOpt("block-size")
                .withDescription("use SIZE-byte blocks")
                .hasArg()
                .withArgName("SIZE")
                .create());
        options.addOption("B", "ignore-backups", false, "do not list implied entried ending with ~");
        options.addOption("c", false, "with -lt: sort by, and show, ctime (time of last modification of file status information) with -l:show ctime and sort by name otherwise: sort by ctime");
        options.addOption("C", false, "list entries by columns");

        String[] args = new String[]{"--a", "hello", "--block-size=10", "-all", "world"};

        CommandLine line = parser.parse(options, args);
        assertTrue(line.hasOption("block-size"));
        assertEquals(line.getOptionValue("block-size"), "10");
    }

    @Test
    public void test2() throws ParseException {
        // use the GNU parser
        CommandLineParser parser = new GnuParser();
        Options options = new Options();
        options.addOption("help", false, "print this message");
        options.addOption("projecthelp", false, "print project help information");
        options.addOption("version", false, "print the version information and exit");
        options.addOption("quiet", false, "be extra quiet");
        options.addOption("verbose", false, "be extra verbose");
        options.addOption("debug", false, "print debug information");
        options.addOption("logfile", true, "use given file for log");
        options.addOption("logger", true, "the class which is to perform the logging");
        options.addOption("listener", true, "add an instance of a class as a project listener");
        options.addOption("buildfile", true, "use given buildfile");
        options.addOption(OptionBuilder.withDescription("use value for given property")
                .hasArgs()
                .withValueSeparator()
                .create('D'));
        //, null, true, , false, true );
        options.addOption("find", true, "search for buildfile towards the root of the filesystem and use it");

        String[] args = new String[]{"-buildfile", "mybuild.xml",
                "-Dproperty=value", "-Dproperty1=value1",
                "-Dmq.namesrv=192.168.60.123:9876", "-Dmq.topic=log-monitor", "-Dmq.tags=",
                "-projecthelp"};

        CommandLine line = parser.parse(options, args);

        // check multiple values
        String[] opts = line.getOptionValues("D");
        Assert.assertEquals("property", opts[0]);
        Assert.assertEquals("value", opts[1]);
        Assert.assertEquals("property1", opts[2]);
        Assert.assertEquals("value1", opts[3]);

        // check single value
        Assert.assertEquals(line.getOptionValue("buildfile"), "mybuild.xml");

        // check option
        assertTrue(line.hasOption("projecthelp"));

    }

    @Test
    public void test3() throws ParseException {
        Options options = new Options();
        CommandLine commandLine = CliOptions.initOptions(options, new String[]{"-h"});
        if (commandLine.hasOption('h')) {
            new HelpFormatter().printHelp("assemble", options, true);
        }
    }
}
