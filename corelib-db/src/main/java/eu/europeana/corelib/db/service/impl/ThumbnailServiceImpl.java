/*
 * Copyright 2007-2012 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved 
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 *  
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under 
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of 
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under 
 *  the Licence.
 */

package eu.europeana.corelib.db.service.impl;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import eu.europeana.corelib.db.entity.nosql.Image;
import eu.europeana.corelib.db.entity.nosql.ImageCache;
import eu.europeana.corelib.db.exception.DatabaseException;
import eu.europeana.corelib.db.service.ThumbnailService;
import eu.europeana.corelib.db.service.abstracts.AbstractNoSqlServiceImpl;
import eu.europeana.corelib.definitions.exception.ProblemType;
import eu.europeana.corelib.definitions.model.ThumbSize;
import eu.europeana.corelib.utils.ImageUtils;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 */
public class ThumbnailServiceImpl extends AbstractNoSqlServiceImpl<ImageCache, String> implements ThumbnailService {

	/* (non-Javadoc)
	 * @see eu.europeana.corelib.db.service.ThumbnailService#storeThumbnail(java.lang.String, java.lang.String, java.net.URL)
	 */
	@Override
	public ImageCache storeThumbnail(String objectId, String collectionId, URL url) throws DatabaseException {
		return storeThumbnail(objectId, DEFAULT_IMAGEID, collectionId, url);
	}
	
	/* (non-Javadoc)
	 * @see eu.europeana.corelib.db.service.ThumbnailService#storeThumbnail(java.lang.String, java.lang.String, java.lang.String, java.net.URL)
	 */
	@Override
	public ImageCache storeThumbnail(String objectId, String imageId, String collectionId, URL url)
			throws DatabaseException {

		sanitycheck("storeThumbnail(String objectId, String imageId, String collectionId, BufferedImage originalImage"
				,objectId,imageId,collectionId);
		
		try {
			BufferedImage originalImage = ImageIO.read(url);
			return storeThumbnail(objectId, collectionId, originalImage, url.toString());
		} catch (IOException e) {
			throw new DatabaseException(e, ProblemType.UNKNOWN);
		}
	}

	/* (non-Javadoc)
	 * @see eu.europeana.corelib.db.service.ThumbnailService#storeThumbnail(java.lang.String, java.lang.String, java.awt.image.BufferedImage, java.lang.String)
	 */
	@Override
	public ImageCache storeThumbnail(String objectId, String collectionId, BufferedImage originalImage, String url) throws DatabaseException {
		return storeThumbnail(objectId, DEFAULT_IMAGEID, collectionId, originalImage, url);
	}
	

	/* (non-Javadoc)
	 * @see eu.europeana.corelib.db.service.ThumbnailService#storeThumbnail(java.lang.String, java.lang.String, java.lang.String, java.awt.image.BufferedImage, java.lang.String)
	 */
	@Override
	public ImageCache storeThumbnail(String objectId, String imageId, String collectionId, BufferedImage originalImage,
			String url) throws DatabaseException {

		sanitycheck("storeThumbnail(String objectId, String imageId, String collectionId, BufferedImage originalImage," +
			"String url)",objectId,imageId,collectionId,originalImage);
		
		ImageCache cache = new ImageCache(objectId, imageId, collectionId, url);

		try {
			BufferedImage tiny = ImageUtils.scale(originalImage, ThumbSize.TINY.getMaxWidth(),
					ThumbSize.TINY.getMaxHeight());
			cache.getImages().put(ThumbSize.TINY.toString(), new Image(tiny));

			BufferedImage medium = ImageUtils.scale(originalImage, ThumbSize.MEDIUM.getMaxWidth(),
					ThumbSize.MEDIUM.getMaxHeight());
			cache.getImages().put(ThumbSize.MEDIUM.toString(), new Image(medium));

			BufferedImage large = ImageUtils.scale(originalImage, ThumbSize.LARGE.getMaxWidth(),
					ThumbSize.LARGE.getMaxHeight());
			cache.getImages().put(ThumbSize.LARGE.toString(), new Image(large));
		} catch (IOException e) {
			throw new DatabaseException(e, ProblemType.UNKNOWN);
		}

		return store(cache);
	}
	
	/* (non-Javadoc)
	 * @see eu.europeana.corelib.db.service.ThumbnailService#retrieveThumbnail(java.lang.String, eu.europeana.corelib.definitions.model.ThumbSize)
	 */
	@Override
	public byte[] retrieveThumbnail(String objectId, ThumbSize size) {
		return retrieveThumbnail(objectId, DEFAULT_IMAGEID, size);
	}
	
	/* (non-Javadoc)
	 * @see eu.europeana.corelib.db.service.ThumbnailService#retrieveThumbnail(java.lang.String, java.lang.String, eu.europeana.corelib.definitions.model.ThumbSize)
	 */
	@Override
	public byte[] retrieveThumbnail(String objectId, String imageId, ThumbSize size) {
		
		sanitycheck("retrieveThumbnail(String objectId, String imageId, ThumbSize size)",objectId,imageId);
		
		ImageCache cache = findByID(objectId, imageId);
		if (cache != null) {
			return cache.getImages().get(size.toString()).getImage();
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see eu.europeana.corelib.db.service.ThumbnailService#findByOriginalUrl(java.lang.String)
	 */
	@Override
	public ImageCache findByOriginalUrl(String url) throws DatabaseException {
		return getDao().findOne("originalUrl", url);
	}
	
	/* (non-Javadoc)
	 * @see eu.europeana.corelib.db.service.abstracts.AbstractNoSqlServiceImpl#findByID(java.io.Serializable)
	 */
	@Override
	public ImageCache findByID(String objectId) {
		return findByID(objectId, DEFAULT_IMAGEID);
	}
	
	/* (non-Javadoc)
	 * @see eu.europeana.corelib.db.service.ThumbnailService#findByID(java.lang.String, java.lang.String)
	 */
	@Override
	public ImageCache findByID(String objectId, String imageId) {
		return super.findByID(getId(objectId, imageId));
	}
	
	/* (non-Javadoc)
	 * @see eu.europeana.corelib.db.service.abstracts.AbstractNoSqlServiceImpl#exists(java.io.Serializable)
	 */
	@Override
	public boolean exists(String objectId) {
		return exists(objectId, DEFAULT_IMAGEID);
	}
	
	/* (non-Javadoc)
	 * @see eu.europeana.corelib.db.service.ThumbnailService#exists(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean exists(String objectId, String imageId) {
		return super.exists(getId(objectId, imageId));
	}
	
	/**
	 * @param objectId
	 * @param imageId
	 * @return
	 */
	private String getId(String objectId, String imageId) {
		return new StringBuilder(objectId).append(COMBINE_CHAR).append(imageId).toString();
	}
	
	/**
	 * Checks whether some of the declared values a null.
	 * Throws an IllegalArgumentException in that case   
	 * 
	 * @param checkables
	 */
	private void sanitycheck(String methodname,Object... checkables){
		int length = checkables.length;
		
		for(int i = 0; i<length; i++ ){
			if (checkables[i] == null){
				StringBuilder sb = new StringBuilder();
				sb.append("One of the arguments provided for method ");
				sb.append(methodname);
				sb.append("had a null value (argument no ");
				sb.append(i);
				sb.append(")");
				throw new IllegalArgumentException(sb.toString());
			}
		}
		
	}
	
}
