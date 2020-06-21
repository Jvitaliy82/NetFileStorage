package com.netfilestorage.server;

import com.netfilestorage.common.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.TreeMap;

public class MainHandler extends ChannelInboundHandlerAdapter {

    private final String SERVER_PATH = "server_storage/";

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg instanceof FileRequest) {
                FileRequest fr = (FileRequest) msg;
                if (Files.exists(Paths.get(SERVER_PATH + fr.getFilename()))) {
                    FileMessage fm = new FileMessage(Paths.get(SERVER_PATH + fr.getFilename()));
                    ctx.writeAndFlush(fm);
                }

            } else if (msg instanceof FileMessage) {
                FileMessage fm = (FileMessage) msg;
                Files.write(Paths.get(SERVER_PATH + fm.getFilename()), fm.getData(), StandardOpenOption.CREATE);

            } else if (msg instanceof RefreshMessage) {
                Files.walkFileTree(Paths.get(SERVER_PATH), new SimpleFileVisitor<Path>() {
                    public FileVisitResult visitFile (Path file, BasicFileAttributes attrs) {
                        TreeMap<String, Long> findFiles = new TreeMap<>();
                        String fileName = file.getFileName().toString();
                        Long fileSize = (long) Math.ceil(file.toFile().length() / 1024.0);
                        findFiles.put(fileName, fileSize);
                        RefreshServerStorageMessage refreshSrvFileListMessage = new RefreshServerStorageMessage(findFiles);
                        ctx.writeAndFlush(refreshSrvFileListMessage);
                        return FileVisitResult.CONTINUE;
                    }
                });

            } else if (msg instanceof FileDeleteMessage) {
                FileDeleteMessage fileDeleteMessage = (FileDeleteMessage) msg;
                Files.delete(Paths.get(SERVER_PATH + fileDeleteMessage.getFileName()));

            } else if (msg instanceof AuthMessage) {
                AuthMessage message = (AuthMessage) msg;
                AuthService.connect();
                if (AuthService.checkUser(message.getLogin())) {
                    if (AuthService.checkPassword(message.getLogin(), message.getPassword())) {
                        ctx.writeAndFlush("User checked" + message.getLogin());
                    } else {
                        ctx.writeAndFlush("Incorrect username or password");
                    }
                } else {
                    ctx.writeAndFlush("Incorrect username or password");
                }

            } else if (msg instanceof RegMessage) {
                RegMessage message = (RegMessage) msg;
                AuthService.connect();
                if (AuthService.checkUser(message.getLogin())) {
                    ctx.writeAndFlush("Such user already exists");
                } else {
                    AuthService.addUser(message.getLogin(), message.getPassword());
                    ctx.writeAndFlush("Registration successful");
                }
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
