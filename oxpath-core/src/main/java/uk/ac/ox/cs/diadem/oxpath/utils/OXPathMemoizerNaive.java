package uk.ac.ox.cs.diadem.oxpath.utils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.ox.cs.diadem.webapi.dom.DOMDocument;
import uk.ac.ox.cs.diadem.webapi.dom.DOMNode;

/**
 * 
 * This class is a general java memoizer that was retrieved from
 * <tt>http://onjava.com/pub/a/onjava/2003/08/20/memoization.html?page=2</tt> on 24 Sept 2011. It is useful in OXPath
 * for both the memoization feature in eval_ as well as the extraction marker creation.
 * <p>
 * The class here has been adapted from its original verison to update generics to parameterized types (rather than the
 * raws of pre-Java 1.5). In addition, for efficiency, we memoize methods only whose first parameter is a
 * {@code DOMNode} instance (each such method signature, then, must be unique as we don't account for the name). This
 * way, we can cache by the containing document, rather than the method.
 * 
 * @author Tom White
 * @author AndrewJSel
 * 
 */
public class OXPathMemoizerNaive implements InvocationHandler, OXPathCache {
  // public static OXPathCache memoize(OXPathCache cache) {
  // return (OXPathCache) Proxy.newProxyInstance(
  // cache.getClass().getClassLoader(),
  // cache.getClass().getInterfaces(),
  // new OXPathMemoizer(cache)
  // );
  // }

  @SuppressWarnings("unchecked")
  public static <T extends OXPathCache> T memoize(final T cache) {
    return (T) Proxy.newProxyInstance(cache.getClass().getClassLoader(), cache.getClass().getInterfaces(),
        new OXPathMemoizerNaive(cache));
  }

  private final OXPathCache object;
  private final Map<Method, Map<List<Object>, Object>> caches = new HashMap<Method, Map<List<Object>, Object>>();

  private OXPathMemoizerNaive(final OXPathCache object) {
    this.object = object;
  }

  @Override
  public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
    if (method.getReturnType().equals(Void.TYPE))
      // Don't cache void methods
      return invoke(method, args);
    else if (method.getName().equals("clear") && (args.length == 1) && (args[0] instanceof DOMDocument))
      return clear((DOMDocument) args[0]);
    else if (args[0] instanceof DOMNode) {
      final Map<List<Object>, Object> cache = getCache(method);
      final List<Object> key = Arrays.asList(args);
      Object value = cache.get(key);

      if ((value == null) && !cache.containsKey(key)) {
        value = this.invoke(method, args);
        cache.put(key, value);
      }
      return value;
    } else
      return this.invoke(method, args);
  }

  private Object invoke(final Method method, final Object[] args) throws Throwable {
    try {
      return method.invoke(object, args);
    } catch (final InvocationTargetException e) {
      throw e.getTargetException();
    }
  }

  private synchronized Map<List<Object>, Object> getCache(final Method m) {
    Map<List<Object>, Object> cache = caches.get(m);
    if (cache == null) {
      cache = Collections.synchronizedMap(new HashMap<List<Object>, Object>());
      caches.put(m, cache);
    }
    return cache;
  }

  @Override
  public Boolean clear(final DOMDocument page) {
    final Object result = caches.remove(page);
    return (result != null);
  }

}