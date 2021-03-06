package edu.sei.eecs.pku.hermes.utils.network;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import edu.sei.eecs.pku.hermes.model.Order;

/**
 * Created by bilibili on 15/11/25.
 */
public class GsonRequest<T> extends Request<T> {
    private final Gson gson;
    private final Class<T> clazz;
    private final Map<String, String> headers;
    private final Response.Listener<T> listener;
    private Map<String, String> params;


    /**
     * Make a GET request and return a parsed object from JSON.
     *
     * @param url    URL of the request to make
     * @param clazz  Relevant class object, for Gson's reflection
     * @param params Map of request params
     */
    public GsonRequest(String url, Class<T> clazz, Map<String, String> params,
                         Response.Listener<T> listener, Response.ErrorListener errorListener) {
        this(Method.GET, url, clazz, params, null, listener, errorListener);
    }

    /**
     * Make a request and return a parsed object from JSON.
     *
     * @param url     URL of the request to make
     * @param clazz   Relevant class object, for Gson's reflection
     * @param headers Map of request headers
     */
    public GsonRequest(int method, String url, Class<T> clazz, Map<String, String> headers,
                         Map<String, String> params,
                         Response.Listener<T> listener, Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.clazz = clazz;
        this.headers = headers;
        this.params = params;
        this.listener = listener;


        final GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(OrderListGson.class, new OrderListDeserializer());
        gsonBuilder.registerTypeAdapter(Order.class, new OrderDeserializer());
        gson = gsonBuilder.create();
    }

    /**
     * @param builder requestBuilder
     */
    public GsonRequest(RequestBuilder builder) {
        this(builder.method, builder.url, builder.clazz, builder.headers,
                builder.params, builder.successListener, builder.errorListener);
    }


    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return headers != null ? headers : super.getHeaders();
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return params != null ? params : super.getParams();
    }

    @Override
    protected void deliverResponse(T response) {
        listener.onResponse(response);
    }


    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        String parsed;
        try {
            parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            if (clazz == null) {
                return (Response<T>) Response.success(parsed,
                        HttpHeaderParser.parseCacheHeaders(response));
            } else {
                return Response.success(gson.fromJson(parsed, clazz),
                        HttpHeaderParser.parseCacheHeaders(response));
            }
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JsonSyntaxException e) {
            return Response.error(new ParseError(e));
        } catch (Exception e) {
            return Response.error(new ParseError(e));
        }

    }

    /**
     * requestBiulder  使用方法参见httpClientRequest
     */
    public static class RequestBuilder {
        private int method = Method.GET;
        private String url;
        private Class clazz;
        private Response.Listener successListener;
        private Response.ErrorListener errorListener;
        private Map<String, String> headers;
        private Map<String, String> params;

        public RequestBuilder url(String url) {
            this.url = url;
            return this;
        }

        public RequestBuilder clazz(Class clazz) {
            this.clazz = clazz;
            return this;
        }

        public RequestBuilder successListener(Response.Listener successListener) {
            this.successListener = successListener;
            return this;
        }

        public RequestBuilder errorListener(Response.ErrorListener errorListener) {
            this.errorListener = errorListener;
            return this;
        }

        public RequestBuilder post() {
            this.method = Method.POST;
            return this;
        }

        public RequestBuilder method(int method) {
            this.method = method;
            return this;
        }

        public RequestBuilder addHeader(String key, String value) {
            if (headers == null)
                headers = new HashMap<>();
            headers.put(key, value);
            return this;
        }

        public RequestBuilder headers(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public RequestBuilder params(Map<String, String> params) {
            this.params = params;
            return this;
        }

        public RequestBuilder addParams(String key, String value) {
            if (params == null) {
                params = new HashMap<>();
            }
            params.put(key, value);
            return this;
        }

        public RequestBuilder addMethodParams(String method) {
            if (params == null) {
                params = new HashMap<>();
                post();
            }
            params.put("method", method);
            return this;
        }

        public GsonRequest build() {

            if (this.method == Method.GET && params != null && params.size() > 0) {
//                this.url += '?';
                for (Map.Entry<String, String> entry : params.entrySet()) {
//                    this.url += entry.getKey() + '=' + entry.getValue() + '&';
                    this.url += '/' + entry.getKey() + '/' + entry.getValue();
                }
//                this.url.substring(0, this.url.length()-1);
            }
            Log.d("GsonRequest", this.url);
            return new GsonRequest(this);
        }
    }

}