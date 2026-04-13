package br.com.erp.api.attribute.application.command;

public record CreateCategoryCommand(String name, Long parentId) {
}
