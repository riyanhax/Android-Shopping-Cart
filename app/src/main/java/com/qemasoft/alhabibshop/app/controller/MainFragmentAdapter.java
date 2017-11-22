package com.qemasoft.alhabibshop.app.controller;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qemasoft.alhabibshop.app.R;
import com.qemasoft.alhabibshop.app.Utils;
import com.qemasoft.alhabibshop.app.model.MyCategory;
import com.qemasoft.alhabibshop.app.model.MyItem;
import com.qemasoft.alhabibshop.app.model.MyPromotion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.qemasoft.alhabibshop.app.AppConstants.findStringByName;
import static com.qemasoft.alhabibshop.app.AppConstants.getHomeExtra;


/**
 * Created by Inzimam on 17-Oct-17.
 */

public class MainFragmentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final static int CATEGORY_VIEW = 0;
    private final static int PROMOTION_VIEW = 1;
    private final static int ITEM_VIEW = 2;

    private List<String> keysStrList;
    private CategoryAdapter categoryAdapter;
    private ItemAdapter itemAdapter;

    private List<Object> myAllItemsList = new ArrayList<>();


    private Context context;
    private Utils utils;


    public MainFragmentAdapter(List<String> keysList) {
        this.keysStrList = keysList;
        prepareData();
        Log.e("AllItemTypeSize = ", myAllItemsList.size() + "");
    }

    @Override
    public int getItemViewType(int position) {

        Log.e("getItemViewType ", myAllItemsList.get(position).getClass() + "");
        Object o = myAllItemsList.get(position);
        if (o instanceof List) {
            for (Object obj : (List) o) {
                if (obj instanceof MyCategory) {
                    Log.e("InsideInstenceof", "Success");
                    return CATEGORY_VIEW;
                }
            }
            return ITEM_VIEW;
        } else if (o instanceof MyPromotion) {
            return PROMOTION_VIEW;
        } else {
            return ITEM_VIEW;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        Log.e("itemType = ", "ViewTypeOnCreate " + viewType);
        switch (viewType) {
            case CATEGORY_VIEW:
                View v1 = inflater.inflate(R.layout.layout_main_frag_categories, parent, false);
                viewHolder = new ViewHolder1(v1);
                break;
            case PROMOTION_VIEW:
                View v2 = inflater.inflate(R.layout.discout_layout, parent, false);
                viewHolder = new ViewHolder2(v2);
                break;
            default:
                View v3 = inflater.inflate(R.layout.layout_main_frag_categories, parent, false);
                viewHolder = new ViewHolder1(v3);
                break;
        }
        this.context = parent.getContext();
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int itemType = holder.getItemViewType();
        Log.e("itemType = ", itemType + "");
        switch (itemType) {
            case CATEGORY_VIEW:
                ViewHolder1 vh1 = (ViewHolder1) holder;
                vh1.getTitle().setText(findStringByName(keysStrList.get(position)));
                vh1.getmRecyclerView().setAdapter(new CategoryAdapter(
                        (List<MyCategory>) myAllItemsList.get(position)));
                break;
            case PROMOTION_VIEW:
                ViewHolder2 vh2 = (ViewHolder2) holder;
                configureViewHolder2(vh2, position);
                break;
            default:
                ViewHolder1 vh3 = (ViewHolder1) holder;
                vh3.getTitle().setText(findStringByName(keysStrList.get(position)));
                vh3.getmRecyclerView().setAdapter(new ItemAdapter(
                        (List<MyItem>)myAllItemsList.get(position)));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return keysStrList.size();
    }


    private void configureViewHolder2(ViewHolder2 vh2, int position) {
        MyPromotion promotion = (MyPromotion) myAllItemsList.get(position);
        if (promotion != null) {
            vh2.getTitle().setText(promotion.getTitle());
            vh2.getDescription().setText(promotion.getDescription());
        }
    }

    private void prepareData() {

        List<MyCategory> myCategoryList = new ArrayList<>();
        List<MyItem> myItemList;

        String responseStr = getHomeExtra();
        try {
            JSONObject responseObject = new JSONObject(responseStr);
            Log.e("JSON_Response", "" + responseObject);
            boolean success = responseObject.optBoolean("success");
            if (success) {
                JSONObject homeObject = responseObject.optJSONObject("home");
                JSONObject modules = homeObject.optJSONObject("modules");

                for (int a = 0; a < keysStrList.size(); a++) {
                    Log.e("KeyStr = ", keysStrList.get(a));
                    if (keysStrList.get(a).equals("categories")) {
                        JSONArray featuredCategories = modules.optJSONArray(keysStrList.get(a));
                        for (int i = 0; i < featuredCategories.length(); i++) {
                            JSONObject categoryObject = featuredCategories.getJSONObject(i);
                            MyCategory myCategory = new MyCategory(categoryObject.optString("category_id"),
                                    categoryObject.optString("name"), categoryObject.optString("thumb"));
                            myCategoryList.add(myCategory);
                        }
                        myAllItemsList.add(myCategoryList);
                    } else if (keysStrList.get(a).equals("promotion")) {
                        JSONArray promotionArray = modules.optJSONArray("promotion");
                        JSONObject promotionObject = promotionArray.optJSONObject(0);
                        MyPromotion myPromotion = new MyPromotion(promotionObject.optString("id"),
                                promotionObject.optString("name"),
                                promotionObject.optString("description"));
                        myAllItemsList.add(myPromotion);

                    } else {
                        myItemList = new ArrayList<>();
                        JSONArray featuredProduct = modules.optJSONArray(keysStrList.get(a));
                        for (int i = 0; i < featuredProduct.length(); i++) {
                            JSONObject productObj = featuredProduct.getJSONObject(i);
                            MyItem myItem = new MyItem(productObj.optString("product_id"),
                                    productObj.optString("name"), productObj.optString("dis"),
                                    productObj.optString("price"), productObj.optString("thumb"));
                            myItemList.add(myItem);
                        }
                        myAllItemsList.add(myItemList);
                    }
                }
            } else {
                Log.e("SuccessFalse", "Within getCategories");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("JSONEx_CatAdapterTest", responseStr);
        }
    }

    public class GenericList<T> extends ArrayList<T> {
        private Class<T> genericType;

        public GenericList(Class<T> c) {
            this.genericType = c;
        }

        public Class<T> getGenericType() {
            return genericType;
        }
    }

}
