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

package eu.europeana.corelib.db.service;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.annotation.Resource;
import javax.imageio.ImageIO;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import eu.europeana.corelib.db.dao.NosqlDao;
import eu.europeana.corelib.db.entity.nosql.Image;
import eu.europeana.corelib.db.entity.nosql.ImageCache;
import eu.europeana.corelib.db.exception.DatabaseException;
import eu.europeana.corelib.definitions.model.ThumbSize;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/corelib-db-context.xml", "/corelib-db-test.xml" })
public class ThumbnailServiceTest {

	@Resource(name="corelib_db_imageDao")
	NosqlDao<ImageCache, String> imageDao;
	
	@Resource
	ThumbnailService thumbnailService;
	
	@Before
	public void setup() {
		imageDao.getCollection().drop();
	}
	
	@Test
	public void storeTest() throws IOException, DatabaseException {
		final String OBJ_ID = "objectTest";
		final String COL_ID = "collectionTest";
		
		Assert.assertTrue("Schema not empty...", imageDao.count() == 0);
		
		BufferedImage image = ImageIO.read( getClass().getResourceAsStream("/images/GREATWAR.jpg") );
		ImageCache cache = thumbnailService.storeThumbnail(OBJ_ID, COL_ID, image, "/images/GREATWAR.jpg");
		
		Assert.assertTrue("Test item does not exists in MongoDB", imageDao.exists("_id",OBJ_ID));
		
		byte[] tiny = thumbnailService.retrieveThumbnail(OBJ_ID, ThumbSize.TINY);
		
		Assert.assertTrue(tiny.length == cache.getImages().get(ThumbSize.TINY.toString()).getImage().length );
		
		ImageCache imageCache = thumbnailService.findByID(OBJ_ID);
		
		Assert.assertEquals(image.getHeight(), imageCache.getHeight());
		Assert.assertEquals(image.getWidth(), imageCache.getWidth());
		
		Image tinyImage = imageCache.getImages().get(ThumbSize.TINY.toString());
		
		Assert.assertTrue(tinyImage.getHeight() > 0);
		Assert.assertEquals(tinyImage.getWidth(), ThumbSize.TINY.getMaxWidth());
		
	}

}
