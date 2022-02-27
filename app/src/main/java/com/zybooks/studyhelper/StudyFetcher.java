package com.zybooks.studyhelper;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class StudyFetcher {

    public interface OnStudyDataReceivedListener {
        void onSubjectsReceived(List<Subject> subjectList);
        void onQuestionsReceived(Subject subject, List<Question> questionList);
        void onErrorResponse(VolleyError error);
    }

    private final String WEBAPI_BASE_URL = "https://wp.zybooks.com/study-helper.php";
    private final String TAG = "StudyFetcher";

    private final RequestQueue mRequestQueue;

    public StudyFetcher(Context context) {
        mRequestQueue = Volley.newRequestQueue(context);
    }

    public void fetchSubjects(final OnStudyDataReceivedListener listener) {

        String url = Uri.parse(WEBAPI_BASE_URL).buildUpon()
                .appendQueryParameter("type", "subjects").build().toString();

        // Request all subjects
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> listener.onSubjectsReceived(jsonToSubjects(response)),
                error -> listener.onErrorResponse(error));

        mRequestQueue.add(request);
    }

    private List<Subject> jsonToSubjects(JSONObject json) {

        // Create a list of subjects
        List<Subject> subjectList = new ArrayList<>();

        try {
            JSONArray subjectArray = json.getJSONArray("subjects");

            for (int i = 0; i < subjectArray.length(); i++) {
                JSONObject subjectObj = subjectArray.getJSONObject(i);

                Subject subject = new Subject(subjectObj.getString("subject"));
                subject.setUpdateTime(subjectObj.getLong("updatetime"));
                subjectList.add(subject);
            }
        }
        catch (Exception e) {
            Log.e(TAG, "Field missing in the JSON data: " + e.getMessage());
        }

        return subjectList;
    }

    public void fetchQuestions(final Subject subject, final OnStudyDataReceivedListener listener) {

        String url = Uri.parse(WEBAPI_BASE_URL).buildUpon()
                .appendQueryParameter("type", "questions")
                .appendQueryParameter("subject", subject.getText())
                .build().toString();

        // Request questions for this subject
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> listener.onQuestionsReceived(subject, jsonToQuestions(response)),
                error -> listener.onErrorResponse(error));

        mRequestQueue.add(jsObjRequest);
    }

    private List<Question> jsonToQuestions(JSONObject json) {

        // Create a list of questions
        List<Question> questionList = new ArrayList<>();

        try {
            JSONArray questionArray = json.getJSONArray("questions");

            for (int i = 0; i < questionArray.length(); i++) {
                JSONObject questionObj = questionArray.getJSONObject(i);

                Question question = new Question();
                question.setText(questionObj.getString("question"));
                question.setAnswer(questionObj.getString("answer"));
                question.setSubjectId(0);
                questionList.add(question);
            }
        }
        catch (JSONException e) {
            Log.e(TAG, "Field missing in the JSON data: " + e.getMessage());
        }

        return questionList;
    }
}