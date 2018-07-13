package com.butel.project.relay.util;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * @author ninghf
 * @version 1.0.0.1
 * @copyright www.butel.com
 * @createtime 2018/7/3
 * @description TODO
 */
public class Wrapper extends HttpServletResponseWrapper {

    public static final int OT_NONE = 0;
    public static final int OT_WRITER = 1;
    public static final int OT_STREAM = 2;
    private int outputType = OT_NONE;
    private ServletOutputStream output = null;
    private PrintWriter writer = null;
    private ByteArrayOutputStream buffer;


    public Wrapper(HttpServletResponse response) {
        super(response);
        buffer = new ByteArrayOutputStream();
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (outputType == OT_WRITER)
            throw new IllegalStateException();
        else if (outputType == OT_STREAM)
            return output;
        else {
            outputType = OT_STREAM;
            output = new WrappedOutputStream(buffer);
            return output;
        }
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (outputType == OT_STREAM)
            throw new IllegalStateException();
        else if (outputType == OT_WRITER)
            return writer;
        else {
            outputType = OT_WRITER;
            writer = new PrintWriter(new OutputStreamWriter(buffer, getCharacterEncoding()));
            return writer;
        }
    }

    @Override
    public void flushBuffer() throws IOException {
        if (outputType == OT_WRITER)
            writer.flush();
        if (outputType == OT_STREAM)
            output.flush();
    }

    @Override
    public void reset() {
        outputType = OT_NONE;
        buffer.reset();
    }

    public byte[] getResponseData() throws IOException {
        flushBuffer();
        return buffer.toByteArray();
    }

    class WrappedOutputStream extends ServletOutputStream {

        private ByteArrayOutputStream buffer;

        public WrappedOutputStream(ByteArrayOutputStream buffer) {
            this.buffer = buffer;
        }

        @Override
        public void write(int b) throws IOException {
            buffer.write(b);
        }

        public byte[] toByteArray() {
            return buffer.toByteArray();
        }

        @Override
        public boolean isReady() {
            // TODO Auto-generated method stub
            return true;
        }

        @Override
        public void setWriteListener(WriteListener listener) {
            // TODO Auto-generated method stub

        }

    }
}
