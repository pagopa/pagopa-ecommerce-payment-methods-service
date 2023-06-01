resource "azuread_application" "main" {
  display_name = "github-${local.github.org}-${local.github.repository}-${var.env}-main"
}

resource "azuread_service_principal" "main" {
  application_id = azuread_application.main.application_id
}

resource "azuread_application_federated_identity_credential" "main" {
  application_object_id = azuread_application.main.object_id
  display_name          = "github-federated"
  description           = "github-federated"
  audiences             = ["api://AzureADTokenExchange"]
  issuer                = "https://token.actions.githubusercontent.com"
  subject               = "repo:${local.github.org}/${local.github.repository}:environment:${var.env}"
}

output "azure_main" {
  value = {
    app_name       = "github-${local.github.org}-${local.github.repository}-${var.env}"
    client_id      = azuread_service_principal.main.application_id
    application_id = azuread_service_principal.main.application_id
    object_id      = azuread_service_principal.main.object_id
  }
}