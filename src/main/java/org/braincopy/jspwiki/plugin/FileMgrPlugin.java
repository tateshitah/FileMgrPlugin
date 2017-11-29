/**
 * 
 */
package org.braincopy.jspwiki.plugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.wiki.PageManager;
import org.apache.wiki.WikiContext;
import org.apache.wiki.WikiEngine;
import org.apache.wiki.WikiPage;
import org.apache.wiki.api.exceptions.PluginException;
import org.apache.wiki.api.exceptions.ProviderException;
import org.apache.wiki.api.exceptions.WikiException;
import org.apache.wiki.api.plugin.WikiPlugin;
import org.apache.wiki.attachment.Attachment;
import org.apache.wiki.attachment.AttachmentManager;

/**
 * @author Hiroaki Tateshita
 *
 */
public class FileMgrPlugin implements WikiPlugin {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.wiki.api.plugin.WikiPlugin#execute(org.apache.wiki.WikiContext,
	 * java.util.Map)
	 */
	public String execute(WikiContext context, Map<String, String> arg1) throws PluginException {
		String result = "Execute result:<br>";
		WikiEngine engine = context.getEngine();

		String[] keywords = new String[2];
		keywords[0] = "http://image02.wiki.livedoor.jp/h/3/hirotate1103/";
		keywords[1] = "http://image01.wiki.livedoor.jp/h/3/hirotate1103/";

		PageManager manager = engine.getPageManager();
		Iterator<WikiPage> pageListIte = null;
		WikiPage currentPage = null;
		String pureText = null;
		try {
			Collection<WikiPage> allPageList = manager.getAllPages();
			pageListIte = allPageList.iterator();
			String picURL = "";
			String picFileName = "";
			String[] tempStrArray = null;
			while (pageListIte.hasNext()) {
				currentPage = pageListIte.next();
				result += currentPage.getName() + " will be checked<br>";
				pureText = engine.getPureText(currentPage);
				if (pureText.contains(keywords[0]) || pureText.contains(keywords[1])) {
					while (pureText.contains(keywords[0])) {
						picURL = pureText.substring(pureText.indexOf(keywords[0]));
						picURL = picURL.substring(0, picURL.indexOf("'"));
						tempStrArray = picURL.split("/");
						picFileName = tempStrArray[tempStrArray.length - 1];
						pureText = pureText.replace(picURL, picFileName);
						getAndSavePic(engine, currentPage, picURL, picFileName);
						// result += currentPage.getName() + "has link(s) to image: " + picURL + "
						// <br>";
					}
					while (pureText.contains(keywords[1])) {
						picURL = pureText.substring(pureText.indexOf(keywords[1]));
						picURL = picURL.substring(0, picURL.indexOf("'"));
						tempStrArray = picURL.split("/");
						picFileName = tempStrArray[tempStrArray.length - 1];
						pureText = pureText.replace(picURL, picFileName);
						getAndSavePic(engine, currentPage, picURL, picFileName);
						// result += currentPage.getName() + "has link(s) to image: " + picURL + "
						// <br>";
					}
					manager.putPageText(currentPage, pureText);
					// engine.saveText(context, pureText);
				}
			}
		} catch (ProviderException e1) {
			e1.printStackTrace();
		} catch (WikiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * 
	 * @param engine
	 * @param currentPage
	 * @param picURL
	 * @param picFileName
	 */
	private void getAndSavePic(WikiEngine engine, WikiPage currentPage, String picURL, String picFileName) {
		HttpClient client = new HttpClient();
		HttpMethod method = new GetMethod(picURL);
		InputStream is = null;
		FileOutputStream fos = null;
		byte[] b = new byte[1024];
		int status;
		File uploadFile = null;
		AttachmentManager am = engine.getAttachmentManager();
		Attachment attachment = null;
		File downloadDir = new File(engine.getWorkDir() + File.separator + currentPage.getName());

		try {
			status = client.executeMethod(method);
			if (status >= 200 && status < 300) {
				// if (!downloadDir.exists()) {
				is = method.getResponseBodyAsStream();
				downloadDir.mkdirs();
				uploadFile = new File(
						engine.getWorkDir() + File.separator + currentPage.getName() + File.separator + picFileName);
				fos = new FileOutputStream(uploadFile);
				int len = 0;
				while ((len = is.read(b)) != -1) {
					fos.write(b, 0, len);
				}
				// } else {
				// uploadFile = new File(engine.getWorkDir() + File.separator +
				// currentPage.getName() + File.separator
				// + picFileName);
				// }
				attachment = new Attachment(engine, currentPage.getName(), picFileName);
				am.storeAttachment(attachment, uploadFile);
			} else {
			}
		} catch (HttpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (is != null) {
			try {
				is.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (fos != null) {
			try {
				fos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
