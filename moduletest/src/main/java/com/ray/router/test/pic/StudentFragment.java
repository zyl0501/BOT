package com.ray.router.test.pic;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ray.router.annotation.Action;
import com.ray.router.annotation.Autowired;
import com.ray.router.launcher.Router;

/**
 * Created by qq448 on 2017/9/21.
 */
@Action(path = "/student_fragment")
public class StudentFragment extends Fragment {
  @Autowired(name = "id")
  int studentId;

  TextView infoView;

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    Router.I().inject(this);
    View view = inflater.inflate(R.layout.fragment_student, container, false);
    infoView = (TextView) view.findViewById(R.id.student_info_text);

    infoView.append("Student " + studentId);
    return view;
  }
}
