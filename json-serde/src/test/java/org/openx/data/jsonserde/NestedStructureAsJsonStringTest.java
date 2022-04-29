/*======================================================================*
 * Copyright (c) 2011, OpenX Technologies, Inc. All rights reserved.    *
 *                                                                      *
 * Licensed under the New BSD License (the "License"); you may not use  *
 * this file except in compliance with the License. Unless required     *
 * by applicable law or agreed to in writing, software distributed      *
 * under the License is distributed on an "AS IS" BASIS, WITHOUT        *
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.     *
 * See the License for the specific language governing permissions and  *
 * limitations under the License. See accompanying LICENSE file.        *
 *======================================================================*/

package org.openx.data.jsonserde;

import io.starburst.openjson.JSONObject;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.serde.serdeConstants;
import org.apache.hadoop.hive.serde2.objectinspector.StructObjectInspector;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 *
 * @author rcongiu
 */
public class NestedStructureAsJsonStringTest {
     static JsonSerDe instance;

  @Before
  public void setUp() throws Exception {
    initialize();
  }

  static public void initialize() throws Exception {
    instance = new JsonSerDe();
    Configuration conf = null;
    Properties tbl = new Properties();
    // from google video API
    tbl.setProperty(serdeConstants.LIST_COLUMNS, "kind,etag,pageInfo,v_items");
    tbl.setProperty(serdeConstants.LIST_COLUMN_TYPES, "string,string,string,string");
    tbl.setProperty("mapping.v_items" , "items");
    tbl.setProperty("mapping.v_statistics" , "statistics");

    instance.initialize(conf, tbl);
  }

  @Test
  public void testDeSerializeJsonAsString() throws Exception {
    // Test that timestamp object can be deserialized
    Writable w = new Text("{ \"kind\": \"youtube#videoListResponse\", \"etag\": \"\\\"79S54kzisD_9SOTfQLu_0TVQSpY/mYlS4-ghMGhc1wTFCwoQl3IYDZc\\\"\", \"pageInfo\": { \"totalResults\": 1, \"resultsPerPage\": 1 }, \"items\": [ { \"kind\": \"youtube#video\", \"etag\": \"\\\"79S54kzisD_9SOTfQLu_0TVQSpY/A4foLs-VO317Po_ulY6b5mSimZA\\\"\", \"id\": \"wHkPb68dxEw\", \"statistics\": { \"viewCount\": \"9211\", \"likeCount\": \"79\", \"dislikeCount\": \"11\", \"favoriteCount\": \"0\", \"commentCount\": \"29\" }, \"topicDetails\": { \"topicIds\": [ \"/m/02mjmr\" ], \"relevantTopicIds\": [ \"/m/0cnfvd\", \"/m/01jdpf\" ] } } ] }");

    JSONObject result = (JSONObject) instance.deserialize(w);

    StructObjectInspector soi = (StructObjectInspector) instance.getObjectInspector();

    assertEquals("youtube#videoListResponse", soi.getStructFieldData(result, soi.getStructFieldRef("kind")));
    assertEquals("\"79S54kzisD_9SOTfQLu_0TVQSpY/mYlS4-ghMGhc1wTFCwoQl3IYDZc\"", soi.getStructFieldData(result, soi.getStructFieldRef("etag")));
    assertEquals("{\"totalResults\":1,\"resultsPerPage\":1}", soi.getStructFieldData(result, soi.getStructFieldRef("pageinfo")));
    assertEquals("[{\"kind\":\"youtube#video\",\"etag\":\"\\\"79S54kzisD_9SOTfQLu_0TVQSpY/A4foLs-VO317Po_ulY6b5mSimZA\\\"\",\"id\":\"wHkPb68dxEw\",\"statistics\":{\"viewCount\":\"9211\",\"likeCount\":\"79\",\"dislikeCount\":\"11\",\"favoriteCount\":\"0\",\"commentCount\":\"29\"},\"topicDetails\":{\"topicIds\":[\"/m/02mjmr\"],\"relevantTopicIds\":[\"/m/0cnfvd\",\"/m/01jdpf\"]}}]",
            soi.getStructFieldData(result, soi.getStructFieldRef("v_items")));
  }

}
