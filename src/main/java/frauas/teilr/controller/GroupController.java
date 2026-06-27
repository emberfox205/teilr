package frauas.teilr.controller;

import frauas.teilr.entity.Group;
import frauas.teilr.entity.User;
import frauas.teilr.service.GroupService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.util.List;

@Controller
@RequestMapping("/groups")
@RequiredArgsConstructor
public class  GroupController {
    private final GroupService groupService;

    /**
     * HTMX: show the group creation form.
     * Called by: hx-get="/groups/new"
     */
    @GetMapping("/new")
    public String newGroupForm() {
        return "fragments/group-form :: groupFormContent";
    }

    /**
     * HTMX: create a new group and return the rendered group card.
     * Called by: hx-post="/groups"
     */
    @PostMapping
    public String createGroup(@RequestParam String name,
                              @RequestParam Long adminId,
                              @RequestParam(required = false) List<Long>memberIds,
                              Model model) {
        List<Long> members = (memberIds != null) ? memberIds : List.of();
        Group created = groupService.createGroup(name, adminId, members);
        List<User> memberList = groupService.getMembersOfGroup(created.getId());
        
        model.addAttribute("group", created);
        model.addAttribute("members", memberList);
        return "fragments/group-card :: groupCardContent";
    }

    /**
     * HTMX: list all groups for a user.
     * Called by: hx-get="/groups?userId=0001"
     */
    @GetMapping
    public String listGroupForUser(@RequestParam Long userId, Model model) {
        List<Group> groups = groupService.getGroupsForUser(userId);
        model.addAttribute("groups", groups);
        return "fragments/group-list :: groupListContent";
    }

    /**
     * HTMX: add a member to a group.
     * Called by: hx-post="/groups/{groupId}/members"
     */
    @PostMapping("/{groupId}/members")
    public String addMember(@PathVariable Long groupId,
                            @RequestParam Long userId,
                            Model model) {
        groupService.addMember(groupId, userId);
        model.addAttribute("groupId", groupId);
        model.addAttribute("members", groupService.getMembersOfGroup(groupId));
        return "fragments/member-list :: memberListContent";
    }

    /**
     * HTMX: delete a group (admin only).
     * Called by: hx-delete="/groups/{groupId}?requesterId=0001"
     */
    @DeleteMapping("/{groupId}")
    @ResponseBody
    public String deleteGroup(@PathVariable Long groupId,
                              @RequestParam Long requesterId) {
        groupService.deleteGroup(groupId, requesterId);
        return ""; // HTMX removes the element from the DOM
    }
}
