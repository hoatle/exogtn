/**
 * Copyright (C) 2011 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.portal.gadget.core;

import com.google.inject.Inject;

import com.google.common.collect.ImmutableMap;

import org.apache.shindig.auth.SecurityTokenCodec;
import org.apache.shindig.common.uri.Uri;
import org.apache.shindig.gadgets.Gadget;
import org.apache.shindig.gadgets.GadgetContext;
import org.apache.shindig.gadgets.http.HttpResponse;
import org.apache.shindig.gadgets.process.ProcessingException;
import org.apache.shindig.gadgets.process.Processor;
import org.apache.shindig.gadgets.servlet.GadgetsHandlerApi;
import org.apache.shindig.gadgets.servlet.GadgetsHandlerService;
import org.apache.shindig.gadgets.spec.Feature;
import org.apache.shindig.gadgets.spec.GadgetSpec;
import org.apache.shindig.gadgets.spec.LinkSpec;
import org.apache.shindig.gadgets.spec.ModulePrefs;
import org.apache.shindig.gadgets.spec.UserPref;
import org.apache.shindig.gadgets.spec.View;
import org.apache.shindig.gadgets.spec.UserPref.EnumValuePair;
import org.apache.shindig.gadgets.uri.IframeUriManager;
import org.apache.shindig.protocol.conversion.BeanDelegator;
import org.apache.shindig.protocol.conversion.BeanFilter;

import java.util.Map;
import java.util.Set;

/**
 * @author <a href="kien.nguyen@exoplatform.com">Kien Nguyen</a>
 * @version $Revision$
 */
public class ExoGadgetsHandlerService extends GadgetsHandlerService
{
   static final Map<Class<?>, Class<?>> apiClasses =
      new ImmutableMap.Builder<Class<?>, Class<?>>()
          .put(View.class, ExoGadgetsHandlerApi.View.class)
          .put(UserPref.class, GadgetsHandlerApi.UserPref.class)
          .put(EnumValuePair.class, GadgetsHandlerApi.EnumValuePair.class)
          .put(ModulePrefs.class, GadgetsHandlerApi.ModulePrefs.class)
          .put(Feature.class, GadgetsHandlerApi.Feature.class)
          .put(LinkSpec.class, GadgetsHandlerApi.LinkSpec.class)
          // Enums
          .put(View.ContentType.class, GadgetsHandlerApi.ViewContentType.class)
          .put(UserPref.DataType.class, GadgetsHandlerApi.UserPrefDataType.class)
          .build();
   
   static final Map<Enum<?>, Enum<?>> enumConversionMap =
      new ImmutableMap.Builder<Enum<?>, Enum<?>>()
          // View.ContentType mapping
          .putAll(BeanDelegator.createDefaultEnumMap(View.ContentType.class,
              GadgetsHandlerApi.ViewContentType.class))
          // UserPref.DataType mapping
          .putAll(BeanDelegator.createDefaultEnumMap(UserPref.DataType.class,
              GadgetsHandlerApi.UserPrefDataType.class))
          .build();
   
   //eXo custom bean delegator
   protected BeanDelegator exoBeanDelegator;
   
   @Inject
   public ExoGadgetsHandlerService(Processor processor,
      IframeUriManager iframeUriManager, SecurityTokenCodec securityTokenCodec,
      BeanFilter beanFilter)
   {
      super(processor, iframeUriManager, securityTokenCodec, beanFilter);
      this.exoBeanDelegator = new BeanDelegator(apiClasses, enumConversionMap);
   }
   
   /**
    * Get gadget metadata information and iframe url. Support filtering of fields
    * @param request request parameters
    * @return gadget metadata nd iframe url
    * @throws ProcessingException
    */
   @Override
   public GadgetsHandlerApi.MetadataResponse getMetadata(GadgetsHandlerApi.MetadataRequest request)
       throws ProcessingException {
     if (request.getUrl() == null) {
       throw new ProcessingException("Missing url paramater", HttpResponse.SC_BAD_REQUEST);
     }
     if (request.getContainer() == null) {
       throw new ProcessingException("Missing container paramater", HttpResponse.SC_BAD_REQUEST);
     }
     if (request.getFields() == null) {
       throw new ProcessingException("Missing fields paramater", HttpResponse.SC_BAD_REQUEST);
     }
     Set<String> fields = beanFilter.processBeanFields(request.getFields());

     GadgetContext context = new MetadataGadgetContext(request);
     Gadget gadget = processor.process(context);
     String iframeUrl =
         (fields.contains("iframeurl") || fields.contains(BeanFilter.ALL_FIELDS)) ?
             iframeUriManager.makeRenderingUri(gadget).toString() : null;
     Boolean needsTokenRefresh =
         (fields.contains("needstokenrefresh") || fields.contains(BeanFilter.ALL_FIELDS)) ?
             gadget.getAllFeatures().contains("auth-refresh") : null;
     return createMetadataResponse(context.getUrl(), gadget.getSpec(), iframeUrl,
         needsTokenRefresh, fields);
   }
   
   private GadgetsHandlerApi.MetadataResponse createMetadataResponse(
      Uri url, GadgetSpec spec, String iframeUrl, Boolean needsTokenRefresh,
      Set<String> fields) {
    return (GadgetsHandlerApi.MetadataResponse) beanFilter.createFilteredBean(
       exoBeanDelegator.createDelegator(spec, GadgetsHandlerApi.MetadataResponse.class,
            ImmutableMap.<String, Object>of(
                "url", url,
                "error", BeanDelegator.NULL,
                "iframeurl", BeanDelegator.nullable(iframeUrl),
                "needstokenrefresh", BeanDelegator.nullable(needsTokenRefresh))),
        fields);
  }

}
