package com.fsindustry.bach.core.node.store;

import com.fsindustry.bach.core.exception.NodeStoreException;
import com.fsindustry.bach.core.node.model.NodeId;
import com.fsindustry.bach.core.support.RandomAccessFileAdapter;
import com.fsindustry.bach.core.support.SeekableFile;
import com.google.common.io.Files;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;

/**
 * 基于文件存放节点状态
 * 格式：
 * 4字节，currentTerm
 * 4字节，voteFor长度
 * 变长，voteFor内容
 */
@Slf4j
public class FileNodeStore implements NodeStore {

    /**
     * 文件名
     */
    public static final String FILE_NAME = "node.bin";

    /**
     * term在文件中的偏移量
     */
    private static final long OFFSET_TERM = 0;

    /**
     * voted_for在文件中的偏移量
     */
    private static final long OFFSET_VOTED_FOR = 4;

    /**
     * 文件操作句柄
     */
    private final SeekableFile seekableFile;

    @Getter
    @Setter
    private int term = 0;

    @Getter
    @Setter
    private NodeId votedFor = null;

    public FileNodeStore(File file) {
        try {
            if (!file.exists()) {
                Files.touch(file);
            }
            seekableFile = new RandomAccessFileAdapter(file);
            initialOrLoad();
        } catch (IOException e) {
            // todo 补充
            log.error("");
            throw new NodeStoreException("");
        }
    }

    public FileNodeStore(SeekableFile file) {
        try {
            this.seekableFile = file;
            initialOrLoad();
        } catch (IOException e) {
            // todo 补充
            log.error("");
            throw new NodeStoreException("");
        }
    }

    private void initialOrLoad() throws IOException {
        // 若文件为空，则初始化文件
        if (seekableFile.size() == 0) {
            seekableFile.truncate(8);
            seekableFile.seek(0);
            // term
            seekableFile.writeInt(0);
            // length of votedFor
            seekableFile.writeInt(0);
        }
        // 若文件非空，则加载
        else {
            term = seekableFile.readInt();
            int length = seekableFile.readInt();
            if (length > 0) {
                byte[] bytes = new byte[length];
                seekableFile.read(bytes);
                votedFor = new NodeId(new String(bytes));
            }
        }
    }

    @Override
    public void close() {
        try {
            seekableFile.close();
        } catch (IOException e) {
            // todo 补充
            log.error("");
            throw new NodeStoreException("");
        }
    }
}
