{{/*
Expand the name of the chart.
*/}}
{{- define "udintsev-hw-chart.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "udintsev-hw-chart.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "udintsev-hw-chart.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "udintsev-hw-chart.labels.app" -}}
helm.sh/chart: {{ include "udintsev-hw-chart.chart" . }}
{{ include "udintsev-hw-chart.selectorLabels.app" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{- define "udintsev-hw-chart.labels.gw" -}}
helm.sh/chart: {{ include "udintsev-hw-chart.chart" . }}
{{ include "udintsev-hw-chart.selectorLabels.gw" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "udintsev-hw-chart.selectorLabels.app" -}}
app.kubernetes.io/name: {{ include "udintsev-hw-chart.name" . }}-app
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}
{{- define "udintsev-hw-chart.selectorLabels.gw" -}}
app.kubernetes.io/name: {{ include "udintsev-hw-chart.name" . }}-gw
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}


{{- define "udintsev-hw-chart.dbFullname" -}}
{{- if .Values.postgresql.fullnameOverride -}}
{{- .Values.postgresql.fullnameOverride | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- $name := default "postgres" .Values.postgresql.nameOverride -}}
{{- if contains $name .Release.Name -}}
{{- .Release.Name | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- end -}}
{{- end }}

{{- define "udintsev-hw-chart.keycloakFullname" -}}
{{- if .Values.keycloak.fullnameOverride -}}
{{- .Values.keycloak.fullnameOverride | trunc 20 | trimSuffix "-" -}}
{{- else -}}
{{- $name := default "keycloak" .Values.keycloak.nameOverride -}}
{{- if contains $name .Release.Name -}}
{{- .Release.Name | trunc 20 | trimSuffix "-" -}}
{{- else -}}
{{- printf "%s-%s" .Release.Name $name | trunc 20 | trimSuffix "-" -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{/*
Override keycloak DB name defined in the keycloak chart
*/}}
{{- define "keycloak.postgresql.fullname" -}}
{{- printf "%s-%s" .Release.Name .Values.postgresql.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Urls
*/}}
{{- define "udintsev-hw-chart.frontendUrl" -}}
{{- printf "%s://%s%s" .Values.ingress.proto .Values.ingress.host (.Values.ingress.pathPrefix | default "") }}
{{- end }}
{{- define "udintsev-hw-chart.keycloakFrontendUrl" -}}
{{- printf "%s%s" (include "udintsev-hw-chart.frontendUrl" .) .Values.ingress.authPath }}
{{- end }}
{{- define "udintsev-hw-chart.jwtIssuerUrl" -}}
{{- printf "%s/realms/%s" (include "udintsev-hw-chart.keycloakFrontendUrl" .) .Values.keycloakRealm }}
{{- end }}

{{- define "udintsev-hw-chart.gwFrontendUrl" -}}
{{- printf "%s%s" (include "udintsev-hw-chart.frontendUrl" .) (.Values.ingress.gwPath | default "") }}
{{- end }}
{{- define "udintsev-hw-chart.gwIngressPath" }}
{{- if .Values.ingress.pathPrefix }}
{{- printf "%s%s($|/)(.*)" .Values.ingress.pathPrefix .Values.ingress.gwPath }}
{{- else }}
{{- printf "%s()(.*)" .Values.ingress.gwPath }}
{{- end }}
{{- end }}
