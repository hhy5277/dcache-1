package org.dcache.webdav;

import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides a handy wrapper around a path.  It provides access to
 * two versions of the same path: the path as a simple String and a
 * URL-encoded version of the path, as defined by RFC 2396 Appendix A.
 * <p>
 * The URL-encoding uses UTF-8 as the underlying character-set.  While this
 * isn't required by RFC 2396 it is the accepted norm.
 * <p>
 * The toString method of this class provides the unencoded form of
 * the string. There are separate methods, getEncoded and
 * getUnencoded, for acquiring the path in its encoded and nonencoded
 * forms respectively.
 * <p>
 * There are some static methods to help with constructing instances of this
 * class.
 */
public class UrlPathWrapper
{
    private static final Logger _log =
        LoggerFactory.getLogger(UrlPathWrapper.class);

    private static final String SCHEME_FILE = "file";
    private static final int SCHEME_FILE_LENGTH = SCHEME_FILE.length();

    private static final UrlPathWrapper EMPTY_PATH = new UrlPathWrapper("", "");

    private final String _path;
    private final String _encoded;

    /**
     * Provide the UrlPathWrapper of an empty path.
     */
    public static UrlPathWrapper forEmptyPath()
    {
        return EMPTY_PATH;
    }


    /**
     * Convert an array of non-URL-encoded path elements into an array of
     * corresponding UrlPathWrapper path elements.
     * @param pathElements the path elements to convert
     * @return the path elements as an array of UrlPathWrapper
     */
    public static UrlPathWrapper[] forPaths(String[] pathElements)
    {
        UrlPathWrapper[] encoded = new UrlPathWrapper[pathElements.length];

        for(int i = 0; i < pathElements.length; i++) {
            encoded [i] = UrlPathWrapper.forPath(pathElements[i]);
        }

        return encoded;
    }


    /**
     * Create a UrlPathWrapper from some unencoded path.
     * @param path the unencoded version of the String.
     * @return the UrlPathWrapper corresponding to this string.
     */
    public static UrlPathWrapper forPath(String path)
    {
        URI uri;

        /*
         * This method contains a work-around for a JRE bug:
         *
         *     https://bugs.openjdk.java.net/show_bug.cgi?id=100223
         *
         * We should be able to use the four-argument constructor to obtain
         * the encoded form of the path element:
         *
         *   uri = new URI(null, null, path, null);
         *   uri.toASCIIString()
         *
         * However, this can fail if the path contains a colon.  Instead, we
         * use the "file" scheme and ensure the path is absolute.  The code
         * then strips off the initial "file:/" to obtain the encoded path.
         */

        try {
            uri = new URI(SCHEME_FILE, null, "/"+path, null);
        } catch (URISyntaxException e) {
            throw new RuntimeException("illegal path element: " +
                    e.getMessage(), e);
        }

        String encoded = uri.toASCIIString();
        int idx = SCHEME_FILE_LENGTH +2; // +2 for ':/' in 'file:/'

        return new UrlPathWrapper(path, encoded.substring(idx,
                encoded.length()));
    }

    private UrlPathWrapper(String path, String encoded)
    {
        _log.debug("building string-pair '{}' and '{}'", path, encoded);
        _path = path;
        _encoded = encoded;
    }

    @Override
    public String toString()
    {
        return _path;
    }

    /**
     *  Provide the path element without any URL-encoding.
     *
     *  This method is accessible using the "unencoded" property in
     *  ANTLR's StringTemplate; for example, if "name" is a
     *  UrlPathWrapper then "name.unencoded" uses the output of this
     *  method.
     */
    public String getUnencoded()
    {
        return _path;
    }

    /**
     *  Provide the path element in an URL-encoded form.
     *
     *  This method is accessible using the "encoded" property in
     *  ANTLR's StringTemplate; for example, if "name" is a
     *  UrlPathWrapper then "name.unencoded" uses the output of this
     *  method.
     */
    public String getEncoded()
    {
        return _encoded;
    }
}
