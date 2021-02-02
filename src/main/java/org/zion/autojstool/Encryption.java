package org.zion.autojstool;

import com.stardust.autojs.engine.encryption.ScriptEncryption;
import com.stardust.autojs.project.BuildInfo;
import com.stardust.autojs.project.ProjectConfig;
import com.stardust.autojs.rhino.TokenStream;
import com.stardust.autojs.script.EncryptedScriptFileHeader;
import com.stardust.autojs.script.JavaScriptFileSource;
import com.stardust.pio.PFiles;
import com.stardust.util.AdvancedEncryptionStandard;
import com.stardust.util.MD5;
import com.stardust.util.MapBuilder;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.Charsets;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.util.Map;

public class Encryption {
    public String mKey;
    public String mInitVector;
    public File mProjectDir;

    Encryption(File projectDir) throws Exception {
        checkProjectDir(projectDir);
        mProjectDir = projectDir;
        String configFilePath = new File(projectDir, ProjectConfig.CONFIG_FILE_NAME).getPath();
        ProjectConfig fromFile = ProjectConfig.fromFile(configFilePath);
        initKey(fromFile);
    }

    public void recursiveDecrypt(File outputDir, File projectDir) throws IOException {
        if (projectDir == null)
        {
            projectDir = mProjectDir;
        }

        if (outputDir.exists() == false) {
            outputDir.mkdirs();
        }

        File[] fs = projectDir.listFiles();
        for(File f:fs){
            if(f.isDirectory())
                recursiveDecrypt(new File(outputDir, f.getName()), f);

            if(f.isFile())
            {
                if (f.getName().endsWith(".js")) {
                    decrypt(outputDir, f);
                }else {
                    File copy = new File(outputDir, f.getName());
                    if (copy.exists()) {
                        copy.delete();
                    }
                    Files.copy(f.toPath(), copy.toPath());
                }
            }
        }
    }

    public void recursiveEncrypt(File outputDir, File projectDir) throws IOException {
        if (projectDir == null)
        {
            projectDir = mProjectDir;
        }

        if (outputDir.exists() == false) {
            outputDir.mkdirs();
        }

        File[] fs = projectDir.listFiles();
        for(File f:fs){
            if(f.isDirectory())
                recursiveEncrypt(new File(outputDir, f.getName()), f);

            if(f.isFile())
            {
                if (f.getName().endsWith(".js")) {
                    encrypt(outputDir, f);
                }else {
                    File copy = new File(outputDir, f.getName());
                    if (copy.exists()) {
                        copy.delete();
                    }
                    Files.copy(f.toPath(), copy.toPath());
                }
            }
        }
    }

    private void checkProjectDir(@NotNull File projectDir) throws Exception {
        if (projectDir.exists() == false) {
            throw new FileNotFoundException(projectDir.getPath());
        }
        if (projectDir.isDirectory() == false) {
            throw new NotDirectoryException(projectDir.getPath());
        }
        File configFile = new File(projectDir, "project.json");
        if (configFile.exists() == false) {
            throw new FileNotFoundException(configFile.getPath());
        }
    }

    // org.autojs.autojs.autojs.build.ApkBuilder
    public void encrypt(File outputDir, File input) throws IOException {
        if (!outputDir.exists()) {
            outputDir.mkdir();
        }
        File output = new File(outputDir, input.getName());
        if (!output.exists()) {
            output.createNewFile();
        }
        encrypt(new FileOutputStream(output), input);
    }

    public void decrypt(File outputDir, File input) throws IOException {
        if (!outputDir.exists()) {
            outputDir.mkdir();
        }
        File output = new File(outputDir, input.getName());
        if (!output.exists()) {
            output.createNewFile();
        }
        decrypt(new FileOutputStream(output), input);
    }

    @NotNull
    public byte[] encrypt(byte[] data) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        try {
            EncryptedScriptFileHeader.INSTANCE.writeHeader(os, (short) parseExecutionMode(data.toString()));
            os.write(new AdvancedEncryptionStandard(mKey.getBytes(), mInitVector).encrypt(data));
            os.close();
        } catch (Exception e) {
            throw new IOException(e);
        }

        return os.toByteArray();
    }

    @NotNull
    public String decrypt(byte[] data) {
        ScriptEncryption scriptEncryption = ScriptEncryption.INSTANCE;
        Intrinsics.checkExpressionValueIsNotNull(data, "bytes");
        byte[] bs = ScriptEncryption.decrypt$default(scriptEncryption, data, 8, 0, 4, null);
        return new String(bs, Charsets.UTF_8);
    }

    // com.stardust.auojs.inrt.autojs.XJavaScriptEngine
    public String decrypt(@NotNull File file) {
        byte[] readBytes = PFiles.readBytes(file.getPath());
        ScriptEncryption scriptEncryption = ScriptEncryption.INSTANCE;
        Intrinsics.checkExpressionValueIsNotNull(readBytes, "bytes");
        byte[] bs = ScriptEncryption.decrypt$default(scriptEncryption, readBytes, 8, 0, 4, (Object) null);
        return new String(bs, Charsets.UTF_8);
    }

    // com.stardust.auojs.inrt.launch.AssetsProjectLauncher
    private void initKey(@NotNull ProjectConfig projectConfig) {
        String md5 = MD5.md5(projectConfig.getPackageName() + projectConfig.getVersionName() + projectConfig.getMainScriptFile());
        StringBuilder sb = new StringBuilder();
        BuildInfo buildInfo = projectConfig.getBuildInfo();
        Intrinsics.checkExpressionValueIsNotNull(buildInfo, "projectConfig.buildInfo");
        sb.append(buildInfo.getBuildId());
        sb.append(projectConfig.getName());
        String md52 = MD5.md5(sb.toString());
        Intrinsics.checkExpressionValueIsNotNull(md52, "MD5.md5(projectConfig.bu…dId + projectConfig.name)");
        if (md52 != null) {
            String substring = md52.substring(0, 16);
            Intrinsics.checkExpressionValueIsNotNull(substring, "(this as java.lang.Strin…ing(startIndex, endIndex)");
            try {
                Field declaredField = ScriptEncryption.class.getDeclaredField("mKey");
                Intrinsics.checkExpressionValueIsNotNull(declaredField, "fieldKey");
                declaredField.setAccessible(true);
                declaredField.set( null, md5);

                mKey = md5;

                Field declaredField2 = ScriptEncryption.class.getDeclaredField("mInitVector");
                Intrinsics.checkExpressionValueIsNotNull(declaredField2, "fieldVector");
                declaredField2.setAccessible(true);
                declaredField2.set(null, substring);

                mInitVector = substring;

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            throw new TypeCastException("null cannot be cast to non-null type java.lang.String");
        }
    }

    private int parseExecutionMode(String script) {
        TokenStream tokenStream = new TokenStream(new StringReader(script), (String) null, 1);
        int i = 0;
        while (true) {
            if (i > 300) {
                break;
            }
            try {
                int token = tokenStream.getToken();
                if (token == 0) {
                    break;
                }
                i++;
                if (token != 1) {
                    if (token != 162) {
                        if (token == 41 && tokenStream.getTokenLength() > 2) {
                            String substring = script.substring(tokenStream.getTokenBeg() + 1, tokenStream.getTokenEnd() - 1);
                            if (tokenStream.getToken() == 83) {
//                                Log.d(LOG_TAG, "string = " + substring);
                                return parseExecutionMode(substring.split(" "));
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }
        return 0;
    }

    private int parseExecutionMode(@NotNull String[] strArr) {
        int i = 0;
        Map<String, Integer> EXECUTION_MODES = new MapBuilder().put("ui", 1).put("auto", 2).build();
        for (String str : strArr) {
            Integer num = EXECUTION_MODES.get(str);
            if (num != null) {
                i |= num.intValue();
            }
        }
        return i;
    }

    // org.autojs.autojs.autojs.build.ApkBuilder
    private void encrypt(FileOutputStream fileOutputStream, File file) throws IOException {
        try {
            EncryptedScriptFileHeader.INSTANCE.writeHeader(fileOutputStream, (short) new JavaScriptFileSource(file).getExecutionMode());
            fileOutputStream.write(new AdvancedEncryptionStandard(mKey.getBytes(), mInitVector).encrypt(PFiles.readBytes(file.getPath())));
            fileOutputStream.close();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    private void decrypt(FileOutputStream fileOutputStream, File file) throws IOException {
        try {
            String js = decrypt(PFiles.readBytes(file.getPath()));
            fileOutputStream.write(js.getBytes(StandardCharsets.UTF_8));
            fileOutputStream.close();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
}
