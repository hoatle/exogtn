/*
 * Copyright (C) 2010 eXo Platform SAS.
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

package org.exoplatform.web.controller.router;

import org.exoplatform.web.controller.QualifiedName;
import static org.exoplatform.web.controller.metadata.DescriptorBuilder.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestPortal extends AbstractTestController
{

   public void testLanguage1() throws Exception
   {
      Router router = router().add(
         route("/public{gtn:lang}").
            with(pathParam("gtn:lang").matchedBy("(/[A-Za-z][A-Za-z])?").preservePath())).
         build();

      //
      assertEquals(Collections.singletonMap(QualifiedName.parse("gtn:lang"), ""), router.route("/public"));
      assertEquals(Collections.singletonMap(QualifiedName.parse("gtn:lang"), "/fr"), router.route("/public/fr"));
   }

   public void testLanguage2() throws Exception
   {
      Router router = router().add(
         route("/{gtn:lang}public").
            with(pathParam("gtn:lang").matchedBy("([A-Za-z]{2}/)?").preservePath())).
         build();

      //
      assertEquals(Collections.singletonMap(QualifiedName.parse("gtn:lang"), ""), router.route("/public"));
      assertEquals(Collections.singletonMap(QualifiedName.parse("gtn:lang"), "fr/"), router.route("/fr/public"));
   }

   public void testLanguage3() throws Exception
   {
      Router router = router().
         add(route("/public")).
         add(route("/{gtn:lang}/public").
            with(pathParam("gtn:lang").matchedBy("([A-Za-z]{2})"))).
         build();

      //
      assertEquals(Collections.<QualifiedName, String>emptyMap(), router.route("/public"));
      assertEquals(Collections.singletonMap(QualifiedName.parse("gtn:lang"), "fr"), router.route("/fr/public"));
   }

   public void testLanguage4() throws Exception
   {
      Router router = router().
         add(route("/public/{gtn:lang}{gtn:sitename}{gtn:path}")
            .with(pathParam("gtn:lang").matchedBy("(?:[A-Za-z]{2}/)?").preservePath())
            .with(pathParam("gtn:path").matchedBy(".*").preservePath())).
         build();

      Map<QualifiedName, String> expectedParameters = new HashMap<QualifiedName, String>();
      expectedParameters.put(QualifiedName.create("gtn", "lang"), "fr/");
      expectedParameters.put(QualifiedName.create("gtn", "sitename"), "classic");
      expectedParameters.put(QualifiedName.create("gtn", "path"), "/home");
      
      //
//      assertEquals(Collections.<QualifiedName, String>emptyMap(), router.route("/public"));
      assertEquals(expectedParameters, router.route("/public/fr/classic/home"));
      
      expectedParameters.put(QualifiedName.create("gtn", "path"), "");
      assertEquals(expectedParameters, router.route("/public/fr/classic"));
      
      expectedParameters.put(QualifiedName.create("gtn", "lang"), "");
      expectedParameters.put(QualifiedName.create("gtn", "path"), "/home");
      assertEquals(expectedParameters, router.route("/public/classic/home"));
   }
}
