package com.bignerdranch.android.locatr;

import android.net.Uri;

/**
 * Created by My on 1/27/2016.
 */
public class GalleryItem {
   private String    mCaption;
   private String    mId;
   private String    mUrl;
   private String    mOwner;

   @Override
   public String toString() {
      return mCaption;
   }

   // this method generates photo page URL
   public Uri getPhotoPageUri() {
      return Uri.parse("http://www.flickr.com/photos").buildUpon().appendPath(mOwner).appendPath(mId).build();
   }

   public String getCaption() {
      return mCaption;
   }

   public void setCaption(String caption) {
      mCaption = caption;
   }

   public String getId() {
      return mId;
   }

   public void setId(String id) {
      mId = id;
   }

   public String getOwner() {
      return mOwner;
   }

   public void setOwner(String owner) {
      mOwner = owner;
   }

   public String getUrl() {
      return mUrl;
   }

   public void setUrl(String url) {
      mUrl = url;
   }
}
