package com.sls.uniparser;

/**
 * Created by Serg on 09.12.2015.
 */
public class CustomUrl {

    private String mHome;

    public String getHome() {
        return mHome;
    }

    public String getPath() {
        return mPath;
    }


    private String mPath;

    public CustomUrl(String home, String path) {
        mPath = path;
        mHome = home;
    }


    @Override
    public String toString()
    {
        return (mHome+mPath);
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj == this)
            return true;

     /* obj ссылается на null */

        if(obj == null)
            return false;

     /* Удостоверимся, что ссылки имеют тот же самый тип */

        if(!(getClass() == obj.getClass()))
            return false;
        else
        {
            CustomUrl tmp = (CustomUrl) obj;
            if(tmp.toString().equals(this.toString()))
                return true;
            else
                return false;
        }
    }

    public int hashCode() {
        char [] arrayChar = toString().toCharArray();
        int result = 0;
        for (char anArrayChar : arrayChar)
            result += anArrayChar * 31 ^ (arrayChar.length - 1);

        return result;
    }

}
