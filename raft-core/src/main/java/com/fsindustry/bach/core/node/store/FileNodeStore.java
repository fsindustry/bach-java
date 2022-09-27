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
import java.nio.charset.StandardCharsets;

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
            log.error("Failed to initial FileNodeStore. term: {}, votedFor: {}", term, votedFor);
            throw new NodeStoreException(String.format("Failed to initial FileNodeStore. term: %s, votedFor: %s", term, votedFor), e);
        }
    }

    public FileNodeStore(SeekableFile file) {
        try {
            this.seekableFile = file;
            initialOrLoad();
        } catch (IOException e) {
            log.error("Failed to initial FileNodeStore. term: {}, votedFor: {}", term, votedFor);
            throw new NodeStoreException(String.format("Failed to initial FileNodeStore. term: %s, votedFor: %s", term, votedFor), e);
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
    public void setTerm(int term) {
        try {
            seekableFile.seek(OFFSET_TERM);
            seekableFile.writeInt(term);
        } catch (IOException e) {
            log.error("Failed to store term: {} to file.", term);
            throw new NodeStoreException(String.format("Failed to store term: %s to file.", term), e);
        }
        this.term = term;
    }

    @Override
    public void setVotedFor(NodeId votedFor) {
        try {
            seekableFile.seek(OFFSET_VOTED_FOR);
            if (null == votedFor) {
                seekableFile.writeInt(0);
                seekableFile.truncate(8);
            } else {
                byte[] payload = votedFor.getValue().getBytes(StandardCharsets.UTF_8);
                seekableFile.writeInt(payload.length);
                seekableFile.write(payload);
            }
        } catch (IOException e) {
            log.error("Failed to store votedFor: {} to file.", votedFor);
            throw new NodeStoreException(String.format("Failed to store votedFor: %s to file.", votedFor), e);
        }
        this.votedFor = votedFor;
    }


    @Override
    public void close() {
        try {
            seekableFile.close();
        } catch (IOException e) {
            log.error("Failed to close FileNodeStore. term: {}, votedFor: {}", term, votedFor);
            throw new NodeStoreException(String.format("Failed to close FileNodeStore. term: %s, votedFor: %s", term, votedFor), e);
        }
    }
}
