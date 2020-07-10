/*package com.inveno.rerank;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import com.alibaba.fastjson.JSONObject;

public class ReRankForTag {

    static class Item implements Comparable<Item>{
        public String contentId = "";
        public HashSet<String> tags = new HashSet<String>();
        public double socre = 0;
        public String title = "";
        @Override
        public int compareTo(Item o) {
            return Double.compare(o.socre, this.socre);
        }
    }
    
    static class Item {
        public String contentId = "";
        public HashSet<String> tags = new HashSet<String>();
    }
    
    ArrayList<Item> cache  = new ArrayList<ReRankForTag.Item>();
    static String[] usefulNatureArr = new String[]{"a", "ad", "aj", "an", "i", "j", "l", "n", "ng", "nr", "nrfg", "nrt", "ns", "nt", "nz", "v", "vd", "vg", "vi", "vn", "vq"};
    
    private static HashMap<String, Integer> usefulNature = new HashMap<String, Integer>(){{
        //专业名词类
          put("gc",8);
          put("gg",5);
          put("gi",7);
          put("gm",5);
          put("gp",5);
          put("nbc",7);
          put("nmc",8);

    //人名类
          put("nr",6);
          put("nr1",5);
          put("nr2",5);
          put("nrf",6);
          put("nrj",6);

    //公司
          put("ntc",8);
          put("ntcf",7);

    //医院
          put("nth",7);

    //地域类
          put("ns",8);
          put("nsf",8);

    //学校
          put("nts",7);
          put("ntu",8);

    //机构组织类
          put("nis",8);
          put("nit",7);
          put("nt",7);
          put("nto",7);

    //生物
          put("nba",9);

    //疾病或健康相关
          put("gb",7);
          put("nhd",9);
          put("nhm",9);

    //职业
          put("nnd",9);

    //职位
          put("nnt",7);

    //行为类
          put("vi",7);
          put("vn",6);

    //较为杂乱的
//          put("j",5);
          put("n",6);
          put("nz",6);
          put("userDefine",7);

    //酒店
          put("ntch",7);

    //银行
          put("ntcb",8);

    //食物
            put("nf",8);
            
    // 其他 不大好，但还是可能有用的
//            put("v",5);
            put("l",5);
//            put("i",5);
//            put("vl",5);
//            put("a",5);
//            put("z",2);
//            put("d",2);
//            put("t",5);
//            put("b",5);
//            put("nrt",2);
//            put("al",2);
//            put("s",3);
//            put("ad",2);
//            put("vf",2);
//            put("bl",2);
//            put("dl",2);
//            put("an",3);
//            put("vd",2);
//            put("nrfg",2);
            
//            for(String s : usefulNatureArr){
//                put(s, 6);
//            }
            
     }};
     
    
    public ArrayList<Item> loadData(String path, String ver) throws IOException{
        ArrayList<Item> result = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(path)), StandardCharsets.UTF_8));
        String line = null;
        int cnt = 0;
        while ( ( line = reader.readLine() ) != null ){
            String[] arr = line.split("\t");
            if(arr.length < 4){
                continue;
            }
            
            Item item = new Item();
            item.contentId = arr[0];
            item.title = arr[3];
            try{
                
                // 使用标签
                Map<String, Map<String, Map<String, Number>>> json = 
                        (Map<String, Map<String, Map<String, Number>>>) JSONObject.parse(arr[1]);
                for(Entry<String, Map<String, Number>> level1map : json.get(ver).entrySet()){
                    item.tags.add(level1map.getKey());
                }
                
                // 使用标题分词
//                List<Term> list = ToAnalysis.parse(item.title);
//                for(Term t : list){
//                    if(t.getName().trim().length() <= 0){
//                        continue;
//                    }
//                    if(usefulNature.containsKey(t.natrue().natureStr) && usefulNature.get(t.natrue().natureStr) >= 6){
//                        item.tags.add(t.getName());
//                    }
//                }
            } catch (Exception e){
                
            }
            
            
            double f0 = 0;
            int f0StrartIdx = arr[2].indexOf("0:");
            if(f0StrartIdx >= 0){
                int f0StopIdx = arr[2].indexOf(" ", f0StrartIdx);
                if(f0StopIdx < 0){
                    f0StopIdx = arr[2].length();
                }
                f0 = Double.parseDouble(arr[2].substring(f0StrartIdx + 2, f0StopIdx));
            }
            
            double f7 = 0;
            int f7StrartIdx = arr[2].indexOf("7:");
            if(f7StrartIdx >= 0){
                int f7StopIdx = arr[2].indexOf(" ", f7StrartIdx);
                if(f7StopIdx < 0){
                    f7StopIdx = arr[2].length();
                }
                f7 = Double.parseDouble(arr[2].substring(f7StrartIdx + 2, f7StopIdx));
            }
            
            System.out.println(item.contentId + "\t" + f7 + "\t" + f0 + "\t" + arr[2]);

            // 线上初选评分
//            item.socre = f0 + 0.4 * f7;
            // 为了看重排效果，调整了排序分数
            item.socre = f7 + 0.4 * f0;
            
            cache.add(item);
            
            cnt++;
        }
//        System.out.println("load items count : " + cnt);
        
        Collections.sort(cache);
        
        return result;
    }
    
    public ArrayList<Item> reRank(ArrayList<Item> list, int windowSize, int start, int stop){
        ArrayList<Item> result = (ArrayList<Item>) list.clone();
        
        int len = result.size();
        
        HashSet<String> tagsInWindow = new HashSet<String>();
        stop = Math.min(stop, len);
        
        for(int i = start; i < stop; i++){
            if( i >= windowSize){
                tagsInWindow.removeAll(result.get(i - windowSize).tags);
            }
            for(int j = i; j < len; j++){
                boolean isContains = false;
                for(String s : result.get(j).tags){
                    isContains = tagsInWindow.contains(s);
                    if(isContains){
                        break;
                    }
                }
                if( ! isContains){
                    if(i != j){ // 需要移动
                        Item backwardItem = result.get(j); // 前移的item
                        result.remove(backwardItem);
                        result.add(i, backwardItem);
                    }
                    break;
                }
            }
            tagsInWindow.addAll(result.get(i).tags);
        }
        
        return result;
    }
    
    public static void main(String[] args) throws IOException {
        ReRankForTag self = new ReRankForTag();
        
        self.loadData("F:\\zhizi\\dubboCore\\src\\test\\java\\com\\inveno\\rerank\\tmp_input_0706_content_3.merge", "v8");
        
//        System.out.println(JSONObject.toJSONString(self.cache));
        
        for(Item i : self.cache){
            System.out.println(i.contentId + "\t" + i.socre + "\t" + i.title + "\t" + JSONObject.toJSONString(i.tags));
        }
        System.out.println("\n\n\n================\n\n\n");
        
        long cur = System.currentTimeMillis();
        Timestamp t0 = new Timestamp(System.currentTimeMillis());
        ArrayList<Item> result = self.reRank(self.cache, 6, 0, 10);
        Timestamp t1 = new Timestamp(System.currentTimeMillis());
        
        long end = System.currentTimeMillis();
        
        for(Item i : result){
            System.out.println(i.contentId + "\t" + i.socre + "\t" + i.title + "\t" + JSONObject.toJSONString(i.tags));
        }
        
        System.out.println("\n\n");
        
        System.out.println(t0);
        System.out.println(t1);
        System.out.println((end - cur));
        
        
    }

}
*/