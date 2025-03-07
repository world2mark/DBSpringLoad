package com.auracoda.dbspringload;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.auracoda.dbspringload.Workloads.WorkloadLibraryEndpoints;
import com.auracoda.dbspringload.Workloads.WorkloadService;

@Controller
@RequestMapping
public class HomePage {

    private final WorkloadLibraryEndpoints MyWorkloads;

    @Autowired
    public HomePage(WorkloadLibraryEndpoints MyWorkloads) {
        this.MyWorkloads = MyWorkloads;
    }

    @GetMapping(path = "HomePage")
    public String homePage(Model model) {
        final List<WorkloadService> workloadList = MyWorkloads.ListWorkloads();
        model.addAttribute("MyWorkloadList", workloadList);
        return "HomePage";
    }

}
