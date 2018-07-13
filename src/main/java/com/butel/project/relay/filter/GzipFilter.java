package com.butel.project.relay.filter;

import com.butel.project.relay.util.Wrapper;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

/**
 * @author ninghf
 * @version 1.0.0.1
 * @copyright www.butel.com
 * @createtime 2018/7/3
 * @description TODO
 */
public class GzipFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse resp = (HttpServletResponse) response;
        HttpServletRequest req = (HttpServletRequest) request;
        if (isGZipEncoding(req)) {
            Wrapper wrapper = new Wrapper(resp);
            chain.doFilter(request, wrapper);
            byte[] gzipData = gzip(wrapper.getResponseData());
            resp.addHeader("Content-Encoding", "gzip");
            resp.setContentLength(gzipData.length);
            ServletOutputStream output = response.getOutputStream();
            output.write(gzipData);
            output.flush();
        } else {
            chain.doFilter(request, response);
        }

    }

    @Override
    public void destroy() {

    }

    /**
     * 判断请求客户端是否支持Gzip
     * @param request
     * @return
     */
    private static boolean isGZipEncoding(HttpServletRequest request) {
        boolean flag = false;
        String encoding = request.getHeader("Accept-Encoding");
        if (encoding.indexOf("gzip") != -1) {
            flag = true;
        }
        return flag;
    }

    /**
     * Gzip压缩算法
     * @param data
     * @return
     */
    private byte[] gzip(byte[] data) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(10240);// 初始化10k
        GZIPOutputStream stream = null;
        try {
            stream = new GZIPOutputStream(outputStream);
            stream.write(data);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                stream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return outputStream.toByteArray();
    }

}
