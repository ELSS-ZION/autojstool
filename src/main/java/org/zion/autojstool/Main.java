package org.zion.autojstool;

import com.beust.jcommander.*;
import java.io.File;

public class Main {

    public class FileConverter implements IStringConverter<File> {
        @Override
        public File convert(String value) {
            return new File(value);
        }
    }

    public static class Args {
        @Parameter(names = { "-o", "--output" }, description = "The name of folder that gets written.", converter = FileConverter.class)
        public File mOutputDir = new File("autojs.out");
    }

    @Parameters(commandDescription = "Decrypt script file.")
    public static class CommandDecrypt {
        @Parameter(description = "<The name of folder that with project.json and encrypted script files in it.>", converter = FileConverter.class)
        public File mFile;
    }

    @Parameters(commandDescription = "Encrypt script file.")
    public static class CommandEncrypt {
        @Parameter(description = "<The name of folder that with project.json and encrypted to-be-encrypted script files in it.>", converter = FileConverter.class)
        public File mFile;
    }

    public static JCommander sJcmdr;
    public static CommandDecrypt sCommandDecrypt = new CommandDecrypt();
    public static CommandEncrypt sCommandEncrypt = new CommandEncrypt();
    public static final String CommandDecryptStr = "D";
    public static final String CommandEncryptStr = "E";
    public static File sProjectDir;

    public static void main(String[] args) throws Exception {
//        String[] argv = { "D", "project"};
        Args parsedArgs = new Args();
        sJcmdr = JCommander.newBuilder()
                .addObject(parsedArgs)
                .addCommand(CommandDecryptStr, sCommandDecrypt)
                .addCommand(CommandEncryptStr, sCommandEncrypt)
                .build();

        sJcmdr.setProgramName("autojstool");

        try {
            sJcmdr.parse(args);
        } catch (MissingCommandException e) {
            sJcmdr.usage();
            return;
        }

        if (sJcmdr.getParsedCommand() == null) {
            sJcmdr.usage();
            return;
        }

        switch (sJcmdr.getParsedCommand()) {
            case CommandDecryptStr:
            {
                sProjectDir = sCommandDecrypt.mFile;
                Encryption encryption = new Encryption(sProjectDir);
                encryption.recursiveDecrypt(parsedArgs.mOutputDir, null);
            }
                break;
            case CommandEncryptStr:
            {
                sProjectDir = sCommandEncrypt.mFile;
                Encryption encryption = new Encryption(sProjectDir);
                encryption.recursiveEncrypt(parsedArgs.mOutputDir, null);
            }
            break;
            default:
                sJcmdr.usage();
                return;
        }
    }
}
