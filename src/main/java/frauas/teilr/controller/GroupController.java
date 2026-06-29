package frauas.teilr.controller;

import frauas.teilr.entity.Group;
import frauas.teilr.entity.User;
import frauas.teilr.service.GroupService;
import frauas.teilr.service.GroupViewService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.http.HttpStatus;
import jakarta.servlet.http.HttpSession;

import java.util.List;

@Controller
@CrossOrigin(origins = "*")
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class  GroupController {
    private final GroupService groupService;
    private final GroupViewService groupViewService;

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
                              @RequestParam(required = false) List<Long>memberIds,
                              HttpSession session,
                              Model model) {
        Long adminId = (Long) session.getAttribute("userId");
        if (adminId == null) return "redirect:/auth/login";

        List<Long> members = (memberIds != null) ? memberIds : List.of();
        groupService.createGroup(name, adminId, members);

        // Re-render the user's whole group list so the new group appears immediately.
        model.addAttribute("groups", groupService.getGroupsForUser(adminId));
        return "fragments/group-list :: groupListContent";
    }

    /**
     * HTMX: list all groups for a user.
     * Called by: hx-get="/groups?userId=0001"
     */
    @GetMapping
    public String listGroupForUser(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/auth/login";

        List<Group> groups = groupService.getGroupsForUser(userId);
        model.addAttribute("groups", groups);
        return "fragments/group-list :: groupListContent";
    }

    /**
     * HTMX: open the full group-detail scene (members, who-owes-whom, bills,
     * settlement actions, activity log). Swapped into #app-scene.
     * Called by: hx-get="/api/groups/{groupId}/view"
     */
    @GetMapping("/{groupId}/view")
    public String viewGroup(@PathVariable Long groupId, HttpSession session, Model model) {
        Long requesterId = (Long) session.getAttribute("userId");
        if (requesterId == null) return "redirect:/auth/login";

        model.addAllAttributes(groupViewService.build(groupId, requesterId));
        return "fragments/group-detail :: sceneContent";
    }

    /**
     * HTMX: add a member to a group (admin only) and re-render the group-detail scene.
     * Called by: hx-post="/api/groups/{groupId}/members?userId={id}"
     */
    @PostMapping("/{groupId}/members")
    public String addMember(@PathVariable Long groupId,
                            @RequestParam Long userId,
                            HttpSession session,
                            Model model) {
        Long requesterId = (Long) session.getAttribute("userId");
        if (requesterId == null) return "redirect:/auth/login";

        groupService.addMember(groupId, userId, requesterId);
        model.addAllAttributes(groupViewService.build(groupId, requesterId));
        return "fragments/group-detail :: sceneContent";
    }

    /**
     * HTMX: delete a group (admin only).
     * Called by: hx-delete="/groups/{groupId}?requesterId=0001"
     */
    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long groupId,
                              HttpSession session) {
        Long requesterId = (Long) session.getAttribute("userId");
        if (requesterId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        groupService.deleteGroup(groupId, requesterId);
        return ResponseEntity.noContent().build(); // HTMX removes the element from the DOM
    }
}
